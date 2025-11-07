(() => {
    const statusIds = {
        NEW: 'new-orders',
        CONFIRMED: 'confirmed-orders',
        CANCELED: 'canceled-orders'
    };

    const state = {
        NEW: new Map(),
        CONFIRMED: new Map(),
        CANCELED: new Map()
    };

    const modal = document.getElementById('order-modal');
    const modalBody = modal.querySelector('.modal-body');
    const modalClose = modal.querySelector('.modal-close');
    let stompClient = null;
    let activeOrderId = null;

    function init() {
        const snapshot = window.APP_DATA || {};
        fillState('NEW', snapshot.newOrders || []);
        fillState('CONFIRMED', snapshot.confirmedOrders || []);
        fillState('CANCELED', snapshot.canceledOrders || []);
        renderAll();
        attachEvents();
        connectWebSocket();
    }

    function fillState(status, orders) {
        orders.forEach(order => state[status].set(order.id, order));
    }

    function renderAll() {
        Object.entries(statusIds).forEach(([status, elementId]) => {
            renderList(status, elementId);
        });
    }

    function renderList(status, elementId) {
        const container = document.getElementById(elementId);
        container.innerHTML = '';
        const orders = Array.from((state[status] || new Map()).values());
        if (orders.length === 0) {
            const hint = document.createElement('li');
            hint.className = 'order-card empty';
            hint.textContent = 'No orders';
            container.appendChild(hint);
            return;
        }
        orders.forEach(order => {
            const card = document.createElement('li');
            card.className = `order-card`;
            card.dataset.orderId = order.id;
            card.innerHTML = `
                <strong>${order.customerName}</strong>
                <small>#${order.id}</small>
                <small>Total: ${formatCurrency(order.totalPrice)} • ${order.totalQuantity} items</small>
                <span class="badge ${order.status.toLowerCase()}">${order.status}</span>
            `;
            card.addEventListener('click', () => openModal(order.id));
            container.appendChild(card);
        });
    }

    function openModal(orderId) {
        const order = getOrder(orderId);
        if (!order) {
            return;
        }
        activeOrderId = orderId;
        modalBody.innerHTML = renderOrderDetails(order);
        modal.classList.remove('hidden');
        wireModalActions(order);
    }

    function renderOrderDetails(order) {
        const itemsRows = order.items.map(item => `
            <tr>
                <td>${item.productName}</td>
                <td>${item.quantity}</td>
                <td>${formatCurrency(item.subtotal)}</td>
            </tr>
        `).join('');

        const actionBlock = order.status === 'NEW' ? `
            <div class="actions">
                <textarea id="order-comment" placeholder="Comment or cancellation reason"></textarea>
                <button class="btn btn-confirm" data-action="confirm">Confirm</button>
                <button class="btn btn-cancel" data-action="cancel">Cancel</button>
            </div>
        ` : '';

        const reasonBlock = order.status === 'CANCELED' && order.cancellationReason
            ? `<p><strong>Reason:</strong> ${order.cancellationReason}</p>`
            : '';

        return `
            <div class="order-details">
                <h3>Order #${order.id}</h3>
                <p><strong>Customer:</strong> ${order.customerName} (${order.email}, ${order.phone})</p>
                <p><strong>Address:</strong> ${order.address}, ${order.city}</p>
                <p><strong>Delivery:</strong> ${order.deliveryMethod} • <strong>Payment:</strong> ${order.paymentMethod}</p>
                <p><strong>Created:</strong> ${order.createdAt}</p>
                <p><strong>Total:</strong> ${formatCurrency(order.totalPrice)} (${order.totalQuantity} items)</p>
                ${reasonBlock}
                <table>
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Qty</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${itemsRows}
                    </tbody>
                </table>
                ${actionBlock}
            </div>
        `;
    }

    function wireModalActions(order) {
        const confirmBtn = modalBody.querySelector('[data-action="confirm"]');
        const cancelBtn = modalBody.querySelector('[data-action="cancel"]');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => {
                const comment = document.getElementById('order-comment').value.trim();
                sendConfirm(order.id, comment);
            });
        }
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                const reason = document.getElementById('order-comment').value.trim();
                if (!reason) {
                    alert('Please describe why the order is canceled.');
                    return;
                }
                sendCancel(order.id, reason);
            });
        }
    }

    function closeModal() {
        modal.classList.add('hidden');
        modalBody.innerHTML = '';
        activeOrderId = null;
    }

    function attachEvents() {
        modalClose.addEventListener('click', closeModal);
        modal.addEventListener('click', (event) => {
            if (event.target === modal) {
                closeModal();
            }
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && !modal.classList.contains('hidden')) {
                closeModal();
            }
        });
    }

    function sendConfirm(orderId, comment) {
        fetch(`/api/orders/${orderId}/confirm`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(comment ? {comment} : {})
        }).then(handleResponse).catch(showError);
    }

    function sendCancel(orderId, reason) {
        fetch(`/api/orders/${orderId}/cancel`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({reason})
        }).then(handleResponse).catch(showError);
    }

    function handleResponse(response) {
        if (!response.ok) {
            return response.json().then(body => { throw new Error(body.error || 'Operation failed'); });
        }
        closeModal();
        return response.json();
    }

    function showError(error) {
        alert(error.message || 'Unexpected error');
    }

    function connectWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.connect({}, () => {
            stompClient.subscribe('/topic/orders', payload => {
                const event = JSON.parse(payload.body);
                applyEvent(event);
            });
        });
    }

    function applyEvent(event) {
        if (!event || !event.order) {
            return;
        }
        const {order} = event;
        Object.values(state).forEach(map => map.delete(order.id));
        if (!state[order.status]) {
            state[order.status] = new Map();
        }
        state[order.status].set(order.id, order);
        renderAll();
        if (activeOrderId === order.id) {
            openModal(order.id);
        }
    }

    function getOrder(orderId) {
        for (const map of Object.values(state)) {
            if (map.has(orderId)) {
                return map.get(orderId);
            }
        }
        return null;
    }

    function formatCurrency(value) {
        if (value == null) {
            return '—';
        }
        return new Intl.NumberFormat(undefined, {
            style: 'currency',
            currency: 'USD'
        }).format(value);
    }

    window.addEventListener('DOMContentLoaded', init);
})();
