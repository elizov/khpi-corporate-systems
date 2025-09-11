export UID  = $(shell id -u)
export GID  = $(shell id -g)
export USER = $(shell id -un)

export DOCKER_IP=$(shell docker run --rm alpine nslookup host.docker.internal | grep 'Address' | egrep '[[:digit:]]{1,3}\.[[:digit:]]{1,3}\.[[:digit:]]{1,3}\.[[:digit:]]{1,3}$$' -o)

ARCH   = $(shell uname -p)
OS	   = $(shell uname -s)
OS_MAC = Darwin

BROWSER_OPEN = xdg-open
ifeq ($(OS), $(OS_MAC))
	BROWSER_OPEN := open
endif

-include .env

.PHONY: help
help:
	@echo 'Usage: make [COMMAND]'
	@echo ''
	@cat ./make/* | sed -n 's/^##//p' | column -t -s ':' |  sed -e 's/^/ /'

include ./make/*
