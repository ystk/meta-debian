UID := $(shell id -u)
ifeq ($(UID),0)
	UID = 1000
endif

export UID

start:
	docker compose run work

refresh:
	# Rebuild docker image
	docker compose build --no-cache work

test: build_test qemu_ptest

build_test:
	docker compose run --rm build_test

qemu_ptest:
	docker compose run --rm qemu_ptest

clean:
	rm -rf ../tests/logs
	rm -rf ../tests/html

cleanall:
	docker compose rm -svf work
	docker compose rm -svf build_test
	docker compose rm -svf qemu_ptest
	docker volume rm -f docker_downloads
	docker rmi -f deby-image

.PHONY: refresh start test build_test qemu_ptest clean cleanall
