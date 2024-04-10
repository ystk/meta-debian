# base recipe: meta-openembedded/meta-networking/recipes-filter/nftables/nftables_0.9.0.bb
# base branch: warrior

SUMMARY = "Netfilter Tables userspace utillites"
SECTION = "net"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d1a78fdd879a263a5e0b42d1fc565e79"

inherit debian-package
require recipes-debian/sources/nftables.inc

DEPENDS = "libmnl libnftnl readline gmp bison-native"

SRC_URI += " \
    file://0001-tests-shell-validate-too-deep-jumpstack-from-basecha.patch \
    file://run-ptest \
"

inherit autotools manpages pkgconfig ptest

PACKAGECONFIG ?= ""
PACKAGECONFIG[manpages] = "--enable--man-doc, --disable-man-doc"

do_install_append() {
	install -m 0755 ${S}/debian/nftables.conf ${D}${sysconfdir}/nftables.conf

	install -d ${D}${systemd_system_unitdir}
	install -m 0644 ${S}/debian/nftables.service ${D}${systemd_system_unitdir}/nftables.service

	rm -rf ${D}${sysconfdir}/nftables

	install -d ${D}${docdir}/nftables/examples
	install -m 0755 ${S}/debian/examples/*.nft ${D}${docdir}/nftables/examples
	install -m 0755 ${S}/files/examples/*.nft ${D}${docdir}/nftables/examples

	install -d ${D}${docdir}/nftables/examples/sysvinit
	install -m 0644 ${S}/debian/examples/sysvinit/nftables.init ${D}${docdir}/nftables/examples/sysvinit
}

FILES_${PN} += "${systemd_system_unitdir}/nftables.service \
                ${docdir}/nftables/examples/*.nft \
                ${docdir}/nftables/examples/sysvinit/* \
                "

ASNEEDED = ""

RRECOMMENDS_${PN} += "kernel-module-nf-tables"

RDEPENDS_${PN}-ptest += "bash findutils make iproute2 iputils-ping procps sed util-linux"

TESTDIR = "tests"

PRIVATE_LIBS_${PN}-ptest_append = " libnftables.so.0"

do_install_ptest() {
    cp -rf ${S}/build-aux ${D}${PTEST_PATH}
    cp -rf ${S}/src ${D}${PTEST_PATH}
    mkdir -p ${D}${PTEST_PATH}/src/.libs
    cp -rf ${B}/src/.libs/* ${D}${PTEST_PATH}/src/.libs
    cp -rf ${B}/src/.libs/nft ${D}${PTEST_PATH}/src/
    cp -rf ${S}/${TESTDIR} ${D}${PTEST_PATH}/${TESTDIR}
    # avoid python dependency
    rm -rf ${D}${PTEST_PATH}/${TESTDIR}/py
    # handle multilib
    sed -i s:@libdir@:${libdir}:g ${D}${PTEST_PATH}/run-ptest
}
