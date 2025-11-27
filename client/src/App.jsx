import React, { useEffect, useState } from 'react';

const apiTarget = (import.meta.env.API_TARGET || 'http://localhost:3100').replace(/\/$/, '');
const API_BASE = apiTarget.endsWith('/api') ? apiTarget : `${apiTarget}/api`;

const fetchApi = (url, options = {}) =>
  fetch(url, {
    credentials: 'include', // keep session cookies for cart/checkout
    ...options,
    headers: { ...(options.headers || {}) },
  });

export default function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({ items: [], totalQuantity: 0, totalPrice: 0 });
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState({ products: false, cart: false });
  const [route, setRoute] = useState(window.location.pathname || '/');
  const [filters, setFilters] = useState({
    minPrice: '',
    maxPrice: '',
    search: '',
    sort: 'price-asc',
  });
  const [checkoutForm, setCheckoutForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    postalCode: '',
    deliveryMethod: '',
    paymentMethod: '',
    cardNumber: '',
    notes: '',
  });
  const [checkoutOptions, setCheckoutOptions] = useState({
    paymentMethods: [],
    deliveryMethods: [],
    cashPaymentMethod: '',
  });
  const [requiresCard, setRequiresCard] = useState(false);
  const [checkoutErrors, setCheckoutErrors] = useState({});
  const [checkoutDraft, setCheckoutDraft] = useState(null);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [checkoutSubmitError, setCheckoutSubmitError] = useState('');
  const [orderDetail, setOrderDetail] = useState(null);
  const [orderLoading, setOrderLoading] = useState(false);
  const [orderError, setOrderError] = useState('');
  const [currentUser, setCurrentUser] = useState(null);
  const initialLoginForm = { username: '', password: '' };
  const [loginForm, setLoginForm] = useState(initialLoginForm);
  const [loginSubmitting, setLoginSubmitting] = useState(false);
  const [loginError, setLoginError] = useState('');
  const initialRegisterForm = {
    username: '',
    email: '',
    password: '',
    age: 18,
    phone: '',
    address: '',
    city: '',
    postalCode: '',
  };
  const [registerForm, setRegisterForm] = useState(initialRegisterForm);
  const [registerSubmitting, setRegisterSubmitting] = useState(false);
  const [registerErrors, setRegisterErrors] = useState({});

  const PHONE_PATTERN = /^[+0-9()\-\s]{7,20}$/;
  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [ordersError, setOrdersError] = useState('');
  const [toast, setToast] = useState(null);
  const [navOpen, setNavOpen] = useState(false);
  const [authToken, setAuthToken] = useState(null);

  useEffect(() => {
    loadProducts();
    loadCheckoutOptions();
    loadCart();
    const storedUser = sessionStorage.getItem('currentUser');
    const storedToken = sessionStorage.getItem('authToken');
    if (storedToken) {
      setAuthToken(storedToken);
    }
    if (storedUser) {
      try {
        setCurrentUser(JSON.parse(storedUser));
      } catch {
        loadCurrentUser(storedToken);
      }
    } else if (storedToken) {
      loadCurrentUser(storedToken);
    }
  }, []);

  useEffect(() => {
    if (route.startsWith('/order/')) {
      const parts = route.split('/');
      const orderId = parts[2];
      if (orderId) {
        loadOrder(orderId);
      }
    }
    if (route.startsWith('/orders/my')) {
      loadMyOrders();
    }
    if (route.startsWith('/checkout/confirm')) {
      if (!checkoutDraft) {
        const storedDraft = sessionStorage.getItem('checkoutDraft');
        if (storedDraft) {
          try {
            setCheckoutDraft(JSON.parse(storedDraft));
          } catch {
            // ignore parse errors
          }
        }
      }
      if (cart.items?.length === 0) {
        const storedCart = sessionStorage.getItem('checkoutCart');
        if (storedCart) {
          try {
            setCart(JSON.parse(storedCart));
          } catch {
            // ignore parse errors
          }
        }
      }
    } else {
      loadCart();
    }
  }, [route]);

  useEffect(() => {
    const handler = () => setRoute(window.location.pathname || '/');
    window.addEventListener('popstate', handler);
    return () => window.removeEventListener('popstate', handler);
  }, []);

  const buildQuery = () => {
    const params = new URLSearchParams();
    if (filters.minPrice) params.set('minPrice', filters.minPrice);
    if (filters.maxPrice) params.set('maxPrice', filters.maxPrice);
    if (filters.search) params.set('search', filters.search);
    const [sortField, sortDirection] = filters.sort.split('-');
    if (sortField) params.set('sortField', sortField);
    if (sortDirection) params.set('sortDirection', sortDirection);
    const qs = params.toString();
    return qs ? `?${qs}` : '';
  };

  const authHeaders = () => {
    return authToken ? { Authorization: `Bearer ${authToken}` } : {};
  };

  const loadProducts = async () => {
    setLoading((prev) => ({ ...prev, products: true }));
    try {
      const response = await fetchApi(`${API_BASE}/products${buildQuery()}`);
      if (!response.ok) throw new Error('Failed to load products');
      setProducts(await response.json());
    } catch (error) {
      setStatus(error.message);
    } finally {
      setLoading((prev) => ({ ...prev, products: false }));
    }
  };

  const loadCart = async () => {
    setLoading((prev) => ({ ...prev, cart: true }));
    try {
      const response = await fetchApi(`${API_BASE}/cart`, { headers: authHeaders() });
      if (!response.ok) throw new Error('Failed to load cart');
      setCart(await response.json());
    } catch (error) {
      setStatus(error.message);
    } finally {
      setLoading((prev) => ({ ...prev, cart: false }));
    }
  };

  const addToCart = async (productId) => {
    try {
      const response = await fetchApi(`${API_BASE}/cart/items`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({ productId }),
      });
      if (!response.ok) throw new Error('Cannot add product to cart');
      const data = await response.json();
      setStatus('');
      await loadCart();
      setToast(data.message || 'Product added to cart');
      setTimeout(() => setToast(null), 3000);
    } catch (error) {
      setStatus(error.message);
    }
  };

  const updateCartQuantity = async (productId, quantity) => {
    if (quantity <= 0) {
      await removeCartItem(productId);
      return;
    }
    try {
      const response = await fetchApi(`${API_BASE}/cart/items/${productId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({ quantity }),
      });
      if (!response.ok) throw new Error('Failed to update quantity');
      await loadCart();
    } catch (error) {
      setStatus(error.message);
    }
  };

  const removeCartItem = async (productId) => {
    try {
      const response = await fetchApi(`${API_BASE}/cart/items/${productId}`, {
        method: 'DELETE',
        headers: authHeaders(),
      });
      if (!response.ok) throw new Error('Failed to remove item');
      await loadCart();
    } catch (error) {
      setStatus(error.message);
    }
  };

  const proceedToCheckout = () => {
    navigate('/checkout');
  };

  const loadCheckoutOptions = async () => {
    try {
      const response = await fetchApi(`${API_BASE}/checkout/options`, { headers: authHeaders() });
      if (!response.ok) return;
      const data = await response.json();
      setCheckoutOptions({
        paymentMethods: data.paymentMethods || [],
        deliveryMethods: data.deliveryMethods || [],
        cashPaymentMethod: data.cashPaymentMethod || '',
      });
    } catch (e) {
      // swallow; non-critical
    }
  };

  const handleCheckoutChange = (field, value) => {
    if (field === 'paymentMethod') {
      const cash = checkoutOptions.cashPaymentMethod;
      setRequiresCard(!cash || value.toLowerCase() !== cash.toLowerCase());
    }
    setCheckoutForm((prev) => ({ ...prev, [field]: value }));
  };

  const submitCheckout = async (e) => {
    e.preventDefault();
    setCheckoutErrors({});
    setCheckoutSubmitError('');
    // Save draft locally; order will be created on confirmation
    setCheckoutDraft(checkoutForm);
    sessionStorage.setItem('checkoutDraft', JSON.stringify(checkoutForm));
    sessionStorage.setItem('checkoutCart', JSON.stringify(cart));
    navigate('/checkout/confirm');
  };

  const confirmOrderNavigation = async () => {
    const draft = checkoutDraft || (() => {
      const stored = sessionStorage.getItem('checkoutDraft');
      if (stored) {
        try { return JSON.parse(stored); } catch { return null; }
      }
      return null;
    })() || checkoutForm;

    const orderId = currentOrder?.id || orderDetail?.id;
    if (orderId) {
      sessionStorage.removeItem('checkoutDraft');
      sessionStorage.removeItem('checkoutCart');
      navigate(`/order/${orderId}`);
      loadCart();
      return;
    }

    try {
      const response = await fetchApi(`${API_BASE}/checkout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify(draft),
      });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        if (data.errors) {
          setCheckoutErrors(data.errors);
        }
        if (data.message && data.message !== 'Validation failed') {
          setCheckoutSubmitError(data.message);
        }
        throw new Error(data.message || 'Checkout failed');
      }
      const order = await response.json();
      setCurrentOrder(order);
      sessionStorage.removeItem('checkoutDraft');
      sessionStorage.removeItem('checkoutCart');
      await loadCart();
      navigate(`/order/${order.id}`);
    } catch (error) {
      // errors already surfaced via field or submit error
    }
  };

  const loadOrder = async (orderId) => {
    setOrderLoading(true);
    setOrderError('');
    try {
      const response = await fetchApi(`${API_BASE}/orders/${orderId}`, { headers: authHeaders() });
      if (!response.ok) {
        throw new Error('Order not found');
      }
      const data = await response.json();
      setOrderDetail(data);
    } catch (error) {
      setOrderError(error.message || 'Failed to load order');
    } finally {
      setOrderLoading(false);
    }
  };

  const loadMyOrders = async () => {
    setOrdersLoading(true);
    setOrdersError('');
    try {
      const response = await fetchApi(`${API_BASE}/orders/my`, { headers: authHeaders() });
      if (!response.ok) {
        throw new Error('Failed to load orders');
      }
      const data = await response.json();
      setOrders(data);
    } catch (error) {
      setOrdersError(error.message || 'Failed to load orders');
    } finally {
      setOrdersLoading(false);
    }
  };

  const handleRegisterChange = (field, value) => {
    setRegisterErrors((prev) => ({ ...prev, [field]: undefined }));
    setRegisterForm((prev) => ({ ...prev, [field]: value }));
  };

  const submitRegister = async (e) => {
    e.preventDefault();
    setRegisterSubmitting(true);
    setRegisterErrors({});
    const errors = validateRegisterForm(registerForm);
    if (Object.keys(errors).length > 0) {
      setRegisterErrors(errors);
      setRegisterSubmitting(false);
      return;
    }

    const sanitized = {
      ...registerForm,
      username: registerForm.username?.trim(),
      email: registerForm.email?.trim(),
      password: registerForm.password,
      age: Number(registerForm.age),
      phone: registerForm.phone?.trim(),
      address: registerForm.address?.trim(),
      city: registerForm.city?.trim(),
      postalCode: registerForm.postalCode?.trim(),
    };
    try {
      const response = await fetchApi(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(sanitized),
      });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        if (data.errors) {
          setRegisterErrors(data.errors);
        }
        if (data.message && !data.errors) {
          const lower = data.message.toLowerCase();
          if (lower.includes('password')) {
            setRegisterErrors({ password: data.message });
          } else if (lower.includes('phone')) {
            setRegisterErrors({ phone: data.message });
          } else if (lower.includes('email')) {
            setRegisterErrors({ email: data.message });
          } else if (lower.includes('age')) {
            setRegisterErrors({ age: data.message });
          } else {
            setRegisterErrors({ username: data.message });
          }
        }
        throw new Error(data.message || 'Registration failed');
      }
      setRegisterErrors({});
      setRegisterForm(initialRegisterForm);
      navigate('/login');
    } catch (err) {
      if (!registerErrors || Object.keys(registerErrors).length === 0) {
        setRegisterErrors({ username: err.message || 'Registration failed' });
      }
    } finally {
      setRegisterSubmitting(false);
    }
  };

  const submitLogin = async (e) => {
    e.preventDefault();
    setLoginSubmitting(true);
    setLoginError('');
    try {
      const response = await fetchApi(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm),
      });
      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Invalid username or password');
      }
      const user = await response.json();
      const userPayload = user.user != null ? user.user : user;
      if (user.token) {
        setAuthToken(user.token);
        sessionStorage.setItem('authToken', user.token);
      }
      setCurrentUser(userPayload);
      sessionStorage.setItem('currentUser', JSON.stringify(userPayload));
      await loadCart();
      setLoginForm(initialLoginForm);
      navigate('/');
    } catch (err) {
      setLoginError(err.message || 'Login failed');
    } finally {
      setLoginSubmitting(false);
    }
  };

  const loadCurrentUser = async (tokenOverride) => {
    try {
      const token = tokenOverride || authToken;
      if (!token) return;
      const response = await fetchApi(`${API_BASE}/auth/me`, { headers: { Authorization: `Bearer ${token}` } });
      if (!response.ok) return;
      const user = await response.json();
      setCurrentUser(user);
      sessionStorage.setItem('currentUser', JSON.stringify(user));
      sessionStorage.setItem('authToken', token);
      setAuthToken(token);
    } catch {
      // ignore
    }
  };

  const handleLogout = async () => {
    try {
      await fetchApi(`${API_BASE}/auth/logout`, { method: 'POST', headers: authHeaders() });
    } catch {
      // ignore
    }
    setCurrentUser(null);
    sessionStorage.removeItem('currentUser');
    setAuthToken(null);
    sessionStorage.removeItem('authToken');
    navigate('/');
    setNavOpen(false);
  };

  const validateRegisterForm = (form) => {
    const errors = {};
    const username = form.username?.trim();
    if (!username) {
      errors.username = 'Username is required';
    } else if (username.length < 3 || username.length > 50) {
      errors.username = 'Username must be between 3 and 50 characters';
    }

    const email = form.email?.trim();
    if (!email) {
      errors.email = 'Email is required';
    }

    const password = form.password || '';
    if (password.length < 4 || password.length > 64) {
      errors.password = 'Password must be between 4 and 64 characters';
    }

    const ageNum = Number(form.age);
    if (Number.isNaN(ageNum) || ageNum < 18 || ageNum > 100) {
      errors.age = 'Age must be between 18 and 100';
    }

    const phone = form.phone?.trim();
    if (phone && !PHONE_PATTERN.test(phone)) {
      errors.phone = 'Phone must be 7-20 digits and may include +, spaces, (), -';
    }

    return errors;
  };

  const navigate = (path) => {
    if (path === route) return;
    window.history.pushState({}, '', path);
    setRoute(path);
  };

  const formatPrice = (value) => {
    if (value == null) return '';
    if (typeof value === 'number') {
      return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    const num = Number(value);
    if (Number.isNaN(num)) return value;
    return num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  const NavLink = ({ to, children }) => (
    <a
      className={`nav-link${route === to ? ' active' : ''}`}
      href={to}
      onClick={(e) => {
        e.preventDefault();
        setNavOpen(false);
        navigate(to);
      }}
    >
      {children}
    </a>
  );

  const renderPage = () => {
    if (route === '/' || route === '/home') {
      return (
        <div className="hero mb-4 text-center">
          <h1 className="mb-3 text-primary">Welcome!</h1>
          <p className="lead text-muted mb-4">The best place to buy your favorite products online.</p>
          <div className="d-flex justify-content-center gap-2">
            <button className="btn btn-primary" onClick={() => navigate('/products')}>
              Browse products
            </button>
            <button className="btn btn-outline-secondary" onClick={() => navigate('/cart')}>
              View cart
            </button>
          </div>
        </div>
      );
    }

    if (route.startsWith('/cart')) {
      return (
        <div className="hero mb-4">
          <h2 className="mb-3">Cart</h2>
          {cart.items?.length ? (
            <>
              <div className="table-responsive">
                <table className="table align-middle cart-table">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>Price</th>
                      <th className="text-center">Quantity</th>
                      <th>Subtotal</th>
                      <th className="text-center">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cart.items.map((item) => (
                      <tr key={item.productId}>
                        <td className="fw-semibold">{item.name}</td>
                        <td>{formatPrice(item.price)} UAH</td>
                        <td className="text-center">
                          <div className="input-group input-group-sm cart-qty">
                            <button
                              className="btn btn-outline-secondary"
                              type="button"
                              onClick={() => updateCartQuantity(item.productId, item.quantity - 1)}
                            >
                              –
                            </button>
                            <input
                              className="form-control text-center"
                              value={item.quantity}
                              onChange={(e) => {
                                const val = parseInt(e.target.value, 10);
                                if (!Number.isNaN(val)) {
                                  updateCartQuantity(item.productId, val);
                                }
                              }}
                            />
                            <button
                              className="btn btn-outline-secondary"
                              type="button"
                              onClick={() => updateCartQuantity(item.productId, item.quantity + 1)}
                            >
                              +
                            </button>
                          </div>
                        </td>
                        <td>{formatPrice(item.subtotal)} UAH</td>
                        <td className="text-center">
                          <button
                            className="btn btn-outline-danger btn-sm"
                            type="button"
                            onClick={() => removeCartItem(item.productId)}
                          >
                            Remove
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="d-flex justify-content-end align-items-center gap-4 mt-3">
                <div className="fw-semibold">
                  Items: {cart.totalQuantity || 0} &nbsp;&nbsp; Total: {formatPrice(cart.totalPrice)} UAH
                </div>
                <button className="btn btn-primary btn-lg" onClick={proceedToCheckout}>Proceed to checkout</button>
              </div>
            </>
          ) : (
            <div className="alert alert-secondary mb-0">Cart is empty.</div>
          )}
        </div>
      );
    }

    if (route.startsWith('/login')) {
      return (
        <div className="hero mb-4 d-flex justify-content-center">
          <div style={{ maxWidth: '700px', width: '100%' }}>
            <h2 className="text-center text-primary mb-4">User Login</h2>
            <form className="d-flex flex-column gap-3" onSubmit={submitLogin}>
              <div>
                <label className="form-label">Username</label>
                <input
                  className="form-control"
                  value={loginForm.username}
                  onChange={(e) => setLoginForm((prev) => ({ ...prev, username: e.target.value }))}
                  required
                />
              </div>
              <div>
                <label className="form-label">Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm((prev) => ({ ...prev, password: e.target.value }))}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary" disabled={loginSubmitting}>
                {loginSubmitting ? 'Logging in...' : 'Login'}
              </button>
              <a className="text-center" href="/register" onClick={(e) => { e.preventDefault(); navigate('/register'); }}>
                Create an account
              </a>
              {loginError && <div className="alert alert-danger mb-0">{loginError}</div>}
            </form>
          </div>
        </div>
      );
    }

    if (route.startsWith('/register')) {
        return (
          <div className="hero mb-4">
            <h2 className="mb-3">Register</h2>
            <form className="row g-3" onSubmit={submitRegister}>
              <div className="col-md-6">
                <label className="form-label">Username</label>
                <input className="form-control" value={registerForm.username} name="username"
                       onChange={(e) => handleRegisterChange('username', e.target.value)} required />
                {registerErrors.username && <div className="text-danger small mt-1">{registerErrors.username}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input type="email" className="form-control" value={registerForm.email}
                       onChange={(e) => handleRegisterChange('email', e.target.value)} required />
                {registerErrors.email && <div className="text-danger small mt-1">{registerErrors.email}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Password</label>
                <input type="password" className="form-control" value={registerForm.password}
                       onChange={(e) => handleRegisterChange('password', e.target.value)} required />
                {registerErrors.password && <div className="text-danger small mt-1">{registerErrors.password}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Age</label>
                <input type="number" min="18" max="100" className="form-control" value={registerForm.age}
                       onChange={(e) => handleRegisterChange('age', e.target.value)} required />
                {registerErrors.age && <div className="text-danger small mt-1">{registerErrors.age}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Phone</label>
                <input className="form-control" value={registerForm.phone} name="phone"
                       onChange={(e) => handleRegisterChange('phone', e.target.value)} />
                {registerErrors.phone && <div className="text-danger small mt-1">{registerErrors.phone}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Address</label>
                <input className="form-control" value={registerForm.address}
                       onChange={(e) => handleRegisterChange('address', e.target.value)} />
                {registerErrors.address && <div className="text-danger small mt-1">{registerErrors.address}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">City</label>
                <input className="form-control" value={registerForm.city}
                       onChange={(e) => handleRegisterChange('city', e.target.value)} />
                {registerErrors.city && <div className="text-danger small mt-1">{registerErrors.city}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Postal code</label>
                <input className="form-control" value={registerForm.postalCode}
                       onChange={(e) => handleRegisterChange('postalCode', e.target.value)} />
                {registerErrors.postalCode && <div className="text-danger small mt-1">{registerErrors.postalCode}</div>}
              </div>
              <div className="col-12 d-flex align-items-center gap-2">
                <button type="submit" className="btn btn-success" disabled={registerSubmitting}>
                  {registerSubmitting ? 'Submitting...' : 'Register'}
                </button>
              </div>
            </form>
          </div>
        );
      }

    if (route.startsWith('/orders')) {
      return (
        <div className="hero mb-4">
          {ordersLoading && <p className="text-muted">Loading orders...</p>}
          {ordersError && <div className="alert alert-danger">{ordersError}</div>}
          {!ordersLoading && !ordersError && (
            <div className="table-responsive">
              <table className="table align-middle">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Date</th>
                    <th>Total</th>
                    <th>Items</th>
                    <th>Payment</th>
                    <th>Delivery</th>
                    <th>Status</th>
                    <th className="text-center">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((o) => (
                    <tr key={o.id}>
                      <td className="fw-semibold">{o.id}</td>
                      <td>{o.createdAt ? o.createdAt.replace('T', ' ').slice(0, 16) : ''}</td>
                      <td>{formatPrice(o.totalPrice)} UAH</td>
                      <td>{o.totalQuantity}</td>
                      <td>{o.paymentMethod}</td>
                      <td>{o.deliveryMethod}</td>
                      <td>
                        <span className="badge bg-secondary text-uppercase">{o.status}</span>
                      </td>
                      <td className="text-center">
                        <button
                          className="btn btn-outline-primary btn-sm"
                          onClick={() => navigate(`/order/${o.id}`)}
                        >
                          View
                        </button>
                      </td>
                    </tr>
                  ))}
                  {orders.length === 0 && (
                    <tr>
                      <td colSpan="8" className="text-center text-muted">No orders yet.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      );
    }

    if (route.startsWith('/order/')) {
      const order = orderDetail;
      if (orderLoading) {
        return (
          <div className="hero mb-4">
            <h2 className="mb-2">Loading order...</h2>
          </div>
        );
      }
      if (orderError || !order) {
        return (
          <div className="hero mb-4">
            <h2 className="mb-2">Order</h2>
            <p className="text-muted">{orderError || 'Order not found'}</p>
            <button className="btn btn-primary" onClick={() => navigate('/products')}>Back to shop</button>
          </div>
        );
      }

      return (
        <div className="hero mb-4">
          <h5 className="text-center mb-4">
            Order number: <strong>{order.id}</strong>
          </h5>
          <div className="row g-4">
            <div className="col-lg-7">
              <div className="card h-100">
                <div className="card-body">
                  <h4 className="card-title mb-3">Delivery information</h4>
                  <dl className="row mb-0">
                    <dt className="col-sm-4">Status</dt>
                    <dd className="col-sm-8"><span className="badge bg-secondary text-uppercase">{order.status}</span></dd>
                    <dt className="col-sm-4">Full name</dt>
                    <dd className="col-sm-8">{order.fullName}</dd>
                    <dt className="col-sm-4">Email</dt>
                    <dd className="col-sm-8">{order.email}</dd>
                    <dt className="col-sm-4">Phone</dt>
                    <dd className="col-sm-8">{order.phone}</dd>
                    <dt className="col-sm-4">Address</dt>
                    <dd className="col-sm-8">{order.address}</dd>
                    <dt className="col-sm-4">Delivery</dt>
                    <dd className="col-sm-8">{order.deliveryMethod}</dd>
                    <dt className="col-sm-4">Payment</dt>
                    <dd className="col-sm-8">{order.paymentMethod}</dd>
                    <dt className="col-sm-4">Notes</dt>
                    <dd className="col-sm-8">{order.notes || '—'}</dd>
                  </dl>
                </div>
              </div>
            </div>
            <div className="col-lg-5">
              <div className="card h-100">
                <div className="card-body">
                  <h4 className="card-title">Order summary</h4>
                  <ul className="list-group list-group-flush mb-3">
                    {order.items.map((item) => (
                      <li key={item.productId} className="list-group-item d-flex justify-content-between">
                        <span>{item.productName} × {item.quantity}</span>
                        <span>{formatPrice(item.subtotal)} UAH</span>
                      </li>
                    ))}
                  </ul>
                  <div className="d-flex justify-content-between fw-bold mb-2">
                    <span>Total items</span>
                    <span>{order.totalQuantity} pcs</span>
                  </div>
                  <div className="d-flex justify-content-between fw-bold mb-3">
                    <span>Paid</span>
                    <span>{formatPrice(order.totalPrice)} UAH</span>
                  </div>
                  {order.createdAt && (
                    <div className="d-flex justify-content-between text-muted small mb-3">
                      <span>Created at</span>
                      <span>{order.createdAt.replace('T', ' ')}</span>
                    </div>
                  )}
                  <button className="btn btn-primary w-100" onClick={() => navigate('/products')}>
                    Continue shopping
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      );
    }

    if (route.startsWith('/checkout')) {
      if (route.startsWith('/checkout/confirm')) {
        const order = currentOrder;
        const draft = checkoutDraft || checkoutForm;
        const summaryItems = order?.items || cart.items || [];
        const totalQuantity = order?.totalQuantity ?? cart.totalQuantity;
        const totalPrice = order?.totalPrice ?? cart.totalPrice;
        const details = order || draft;

        if (!summaryItems.length) {
          return (
            <div className="hero mb-4">
              <h2 className="mb-2 text-primary">Confirm Your Order</h2>
              <p className="text-muted">No order to confirm. Please go back to checkout.</p>
              <button className="btn btn-outline-secondary" onClick={() => navigate('/checkout')}>
                Back to checkout
              </button>
            </div>
          );
        }

        return (
          <div className="hero mb-4">
            <h2 className="mb-3 text-primary">Confirm Your Order</h2>
            <div className="row g-4">
              <div className="col-lg-6">
                <div className="card h-100">
                  <div className="card-body">
                    <h4 className="card-title mb-3">Delivery details</h4>
                    <dl className="row mb-0">
                      <dt className="col-sm-4">Full name</dt>
                      <dd className="col-sm-8">{details.fullName}</dd>
                      <dt className="col-sm-4">Email</dt>
                      <dd className="col-sm-8">{details.email}</dd>
                      <dt className="col-sm-4">Phone</dt>
                      <dd className="col-sm-8">{details.phone}</dd>
                      <dt className="col-sm-4">Address</dt>
                      <dd className="col-sm-8">{details.address}</dd>
                      <dt className="col-sm-4">City</dt>
                      <dd className="col-sm-8">{details.city}</dd>
                      <dt className="col-sm-4">Postal code</dt>
                      <dd className="col-sm-8">{details.postalCode}</dd>
                      <dt className="col-sm-4">Delivery</dt>
                      <dd className="col-sm-8">{details.deliveryMethod}</dd>
                      <dt className="col-sm-4">Payment</dt>
                      <dd className="col-sm-8">{details.paymentMethod}</dd>
                      <dt className="col-sm-4">Notes</dt>
                      <dd className="col-sm-8">{details.notes || '—'}</dd>
                    </dl>
                  </div>
                </div>
              </div>
              <div className="col-lg-6">
                <div className="card h-100">
                  <div className="card-body">
                    <h4 className="card-title">Order summary</h4>
                    <ul className="list-group list-group-flush mb-3">
                      {summaryItems.map((item) => (
                        <li key={item.productId} className="list-group-item d-flex justify-content-between">
                          <span>{item.productName || item.name} × {item.quantity}</span>
                          <span>{formatPrice(item.subtotal || item.unitPrice)} UAH</span>
                        </li>
                      ))}
                    </ul>
                    <div className="d-flex justify-content-between fw-bold mb-2">
                      <span>Total items</span>
                      <span>{totalQuantity} pcs</span>
                    </div>
                    <div className="d-flex justify-content-between fw-bold mb-3">
                      <span>Total amount</span>
                      <span>{formatPrice(totalPrice)} UAH</span>
                    </div>
                    <p className="text-muted small mb-3">
                      By confirming the order you agree to the selected payment and delivery options.
                    </p>
                    <div className="d-flex gap-2">
                      <button className="btn btn-outline-secondary" onClick={() => navigate('/checkout')}>
                        Back and edit
                      </button>
                      <button className="btn btn-success" onClick={confirmOrderNavigation}>
                        Confirm order
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      }

      return (
        <div className="hero mb-4">
          <h2 className="mb-3 text-primary">Checkout</h2>
          <div className="row g-4">
            <div className="col-lg-8">
              <form className="checkout-form" onSubmit={submitCheckout}>
                <h4 className="mb-3">Payment & Delivery Information</h4>
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Full name</label>
                    <input className="form-control" required value={checkoutForm.fullName}
                           onChange={(e) => handleCheckoutChange('fullName', e.target.value)} />
                    {checkoutErrors.fullName && <div className="text-danger small mt-1">{checkoutErrors.fullName}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Email</label>
                    <input type="email" className="form-control" required value={checkoutForm.email}
                           onChange={(e) => handleCheckoutChange('email', e.target.value)} />
                    {checkoutErrors.email && <div className="text-danger small mt-1">{checkoutErrors.email}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Phone</label>
                    <input className="form-control" name="phone" required value={checkoutForm.phone}
                           onChange={(e) => handleCheckoutChange('phone', e.target.value)} />
                    {checkoutErrors.phone && <div className="text-danger small mt-1">{checkoutErrors.phone}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">City</label>
                    <input className="form-control" required value={checkoutForm.city}
                           onChange={(e) => handleCheckoutChange('city', e.target.value)} />
                    {checkoutErrors.city && <div className="text-danger small mt-1">{checkoutErrors.city}</div>}
                  </div>
                  <div className="col-md-8">
                    <label className="form-label">Address</label>
                    <input className="form-control" required value={checkoutForm.address}
                           onChange={(e) => handleCheckoutChange('address', e.target.value)} />
                    {checkoutErrors.address && <div className="text-danger small mt-1">{checkoutErrors.address}</div>}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Postal code</label>
                    <input className="form-control" required value={checkoutForm.postalCode}
                           onChange={(e) => handleCheckoutChange('postalCode', e.target.value)} />
                    {checkoutErrors.postalCode && <div className="text-danger small mt-1">{checkoutErrors.postalCode}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Delivery method</label>
                    <select className="form-select" required value={checkoutForm.deliveryMethod}
                            onChange={(e) => handleCheckoutChange('deliveryMethod', e.target.value)}>
                      <option value="">Select delivery method</option>
                      {checkoutOptions.deliveryMethods.map((opt) => (
                        <option key={opt} value={opt}>{opt}</option>
                      ))}
                    </select>
                    {checkoutErrors.deliveryMethod && <div className="text-danger small mt-1">{checkoutErrors.deliveryMethod}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Payment method</label>
                    <select className="form-select" required value={checkoutForm.paymentMethod}
                            onChange={(e) => handleCheckoutChange('paymentMethod', e.target.value)}>
                      <option value="">Select payment method</option>
                      {checkoutOptions.paymentMethods.map((opt) => (
                        <option key={opt} value={opt}>{opt}</option>
                      ))}
                    </select>
                    {checkoutErrors.paymentMethod && <div className="text-danger small mt-1">{checkoutErrors.paymentMethod}</div>}
                  </div>
                  {requiresCard && (
                    <div className="col-md-6">
                      <label className="form-label">Card number</label>
                      <input className="form-control" value={checkoutForm.cardNumber}
                             onChange={(e) => handleCheckoutChange('cardNumber', e.target.value)} />
                      {checkoutErrors.cardNumber && <div className="text-danger small mt-1">{checkoutErrors.cardNumber}</div>}
                    </div>
                  )}
                  <div className="col-12">
                    <label className="form-label">Notes (optional)</label>
                    <textarea className="form-control" rows="3" value={checkoutForm.notes}
                              onChange={(e) => handleCheckoutChange('notes', e.target.value)} />
                    {checkoutErrors.notes && <div className="text-danger small mt-1">{checkoutErrors.notes}</div>}
                  </div>
                </div>
                <div className="d-flex justify-content-between align-items-center mt-4">
                  <button type="button" className="btn btn-outline-secondary" onClick={() => navigate('/cart')}>
                    Back to cart
                  </button>
                  <button type="submit" className="btn btn-primary">Continue to confirmation</button>
                </div>
                {checkoutSubmitError && <div className="alert alert-info mt-3 mb-0">{checkoutSubmitError}</div>}
              </form>
            </div>
            <div className="col-lg-4">
              <div className="card shadow-sm">
                <div className="card-body">
                  <h4 className="card-title d-flex justify-content-between">
                    <span>Order summary</span>
                    <span className="text-muted small">{cart.totalQuantity || 0} pcs</span>
                  </h4>
                  <ul className="list-group list-group-flush mb-3">
                    {cart.items?.map((item) => (
                      <li key={item.productId} className="list-group-item d-flex justify-content-between">
                        <span>{item.name} × {item.quantity}</span>
                        <span>{formatPrice(item.subtotal)} UAH</span>
                      </li>
                    ))}
                  </ul>
                  <div className="d-flex justify-content-between fw-bold mb-2">
                    <span>Total</span>
                    <span>{formatPrice(cart.totalPrice)} UAH</span>
                  </div>
                  <p className="text-muted small mb-0">After submitting the form you will be able to review and confirm your order details.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      );
    }

    // Products catalog view (default)
    return (
      <>
        <div className="filters-bar">
          <div className="filters-grid">
            <input
              className="form-control"
              placeholder="Min price"
              value={filters.minPrice}
              onChange={(e) => setFilters((prev) => ({ ...prev, minPrice: e.target.value }))}
            />
            <input
              className="form-control"
              placeholder="Max price"
              value={filters.maxPrice}
              onChange={(e) => setFilters((prev) => ({ ...prev, maxPrice: e.target.value }))}
            />
            <input
              className="form-control flex-grow-1"
              placeholder="Search..."
              value={filters.search}
              onChange={(e) => setFilters((prev) => ({ ...prev, search: e.target.value }))}
            />
            <select
              className="form-select"
              value={filters.sort}
              onChange={(e) => setFilters((prev) => ({ ...prev, sort: e.target.value }))}
            >
              <option value="price-asc">Sort by price ↑</option>
              <option value="price-desc">Sort by price ↓</option>
              <option value="name-asc">Name A-Z</option>
              <option value="name-desc">Name Z-A</option>
              <option value="id-asc">ID ↑</option>
              <option value="id-desc">ID ↓</option>
            </select>
            <button className="btn btn-primary" onClick={loadProducts} disabled={loading.products}>
              {loading.products ? 'Applying...' : 'Apply'}
            </button>
          </div>
        </div>
        {status && <div className="alert alert-info status mt-3 mb-0">{status}</div>}

        <div className="catalog-grid">
          {products.map((product) => (
            <div key={product.id} className="product-card">
              <div className="product-thumb">
                <img
                  src={product.imageUrl || 'https://dummyimage.com/640x400/e5e7eb/9ca3af&text=No+image'}
                  alt={product.name}
                  loading="lazy"
                  onError={(e) => {
                    e.currentTarget.onerror = null;
                    e.currentTarget.src = 'https://dummyimage.com/640x400/d1d5db/6b7280&text=Image+unavailable';
                  }}
                />
              </div>
              <h5 className="mb-1">{product.name}</h5>
              <p className="text-muted mb-1">{product.description}</p>
              <p className="text-muted mb-2">{product.category}</p>
              <p className="product-price mb-3">{formatPrice(product.price)} UAH</p>
              <button className="btn btn-success w-100" onClick={() => addToCart(product.id)}>
                Add to cart
              </button>
            </div>
          ))}
          {products.length === 0 && !loading.products && (
            <div className="alert alert-secondary">No products yet. Seed data first.</div>
          )}
        </div>
      </>
    );
  };

  return (
    <>
      <nav className="navbar navbar-dark bg-dark">
        <div className="container-fluid nav-container">
          <a
            className="navbar-brand"
            href="/"
            onClick={(e) => {
              e.preventDefault();
              navigate('/');
            }}
          >
            MyShop
          </a>
          <button
            className="nav-toggle d-lg-none"
            aria-label="Toggle navigation"
            onClick={() => setNavOpen((prev) => !prev)}
          >
            ☰
          </button>
          <div className={`nav-links ${navOpen ? 'open' : ''}`}>
            <ul className="navbar-nav ms-auto flex-lg-row flex-column align-items-lg-center">
              <li className="nav-item"><NavLink to="/products">Products</NavLink></li>
              <li className="nav-item">
                <NavLink to="/cart" className="d-flex align-items-center">
                  Cart
                  <span className="badge bg-secondary ms-2">{cart.totalQuantity || 0}</span>
                </NavLink>
              </li>
              {currentUser ? (
                <>
                  <li className="nav-item"><NavLink to="/orders/my">My Orders</NavLink></li>
                  <li className="nav-item d-flex align-items-center">
                    <span className="nav-link user-label">
                      Hello, {currentUser.username}
                    </span>
                  </li>
                  <li className="nav-item">
                    <a className="nav-link" href="/logout" onClick={(e) => { e.preventDefault(); handleLogout(); }}>
                      Logout
                    </a>
                  </li>
                </>
              ) : (
                <>
                  <li className="nav-item"><NavLink to="/login">Login</NavLink></li>
                  <li className="nav-item"><NavLink to="/register">Register</NavLink></li>
                </>
              )}
            </ul>
          </div>
        </div>
      </nav>

      <main className="page">
        {renderPage()}
      </main>
      {toast && (
        <div className="toast-bottom">
          {toast}
        </div>
      )}
    </>
  );
}
