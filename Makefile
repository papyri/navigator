.PHONY: build test

CI_REGISTRY_IMAGE ?= navigator
CI_COMMIT_SHORT_SHA ?= $(shell basename $(shell git rev-parse --show-toplevel))

build:
	docker build -t $(CI_REGISTRY_IMAGE)/builds:$(CI_COMMIT_SHORT_SHA) .

test:
	docker run -e GITHUB_TOKEN -e GITHUB_USERNAME $(build_tag)
