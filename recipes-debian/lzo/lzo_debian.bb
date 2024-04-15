SUMMARY = "Lossless data compression library"
HOMEPAGE = "http://www.oberhumer.com/opensource/lzo/"
SECTION = "libs"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://src/lzo_init.c;beginline=5;endline=25;md5=9ae697ca01829b0a383c5d2d163e0108"

inherit debian-package
require recipes-debian/sources/lzo2.inc
DEBIAN_UNPACK_DIR = "${WORKDIR}/lzo-${PV}"
FILESEXTRAPATHS =. "${FILE_DIRNAME}/lzo:${COREBASE}/meta/recipes-support/lzo/lzo/:"

SRC_URI += " \
	   file://0001-Use-memcpy-instead-of-reinventing-it.patch \
	   file://0001-Add-pkgconfigdir-to-solve-the-undefine-error.patch \
	   file://run-ptest \
	   "

inherit autotools ptest

EXTRA_OECONF = "--enable-shared"

do_install_ptest() {
	t=${D}${PTEST_PATH}
	cp ${S}/util/check.sh $t
	cp ${B}/minilzo/testmini $t
	for i in tests/align tests/chksum lzotest/lzotest examples/simple
		do cp ${B}/`dirname $i`/.libs/`basename $i` $t; \
	done
}

BBCLASSEXTEND = "native nativesdk"
