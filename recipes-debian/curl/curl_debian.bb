#
# base recipe: meta/recipes-support/curl/curl_7.60.0.bb
# base branch: master
# base commit: a5d1288804e517dee113cb9302149541f825d316
#

SUMMARY = "Command line tool and library for client-side URL transfers"
HOMEPAGE = "http://curl.haxx.se/"

inherit debian-package
require recipes-debian/sources/curl.inc

PR = "r1"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;beginline=8;md5=3a34942f4ae3fbf1a303160714e664ac"

# CVE-2021-22926: only affects builds for MacOS.
CVE_CHECK_WHITELIST = "CVE-2021-22926"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI += " \
    file://temporary-workaround-for-build-error-in-7.64.0-4+deb10u8.patch \
    file://run-ptest \
    file://disable-tests \
"

inherit autotools pkgconfig binconfig multilib_header ptest

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)} gnutls proxy threaded-resolver zlib"
PACKAGECONFIG_class-native = "ipv6 proxy ssl threaded-resolver zlib"
PACKAGECONFIG_class-nativesdk = "ipv6 proxy ssl threaded-resolver zlib"

# 'ares' and 'threaded-resolver' are mutually exclusive
PACKAGECONFIG[ares] = "--enable-ares,--disable-ares,c-ares"
PACKAGECONFIG[dict] = "--enable-dict,--disable-dict,"
PACKAGECONFIG[gnutls] = "--with-gnutls,--without-gnutls,gnutls"
PACKAGECONFIG[gopher] = "--enable-gopher,--disable-gopher,"
PACKAGECONFIG[imap] = "--enable-imap,--disable-imap,"
PACKAGECONFIG[ipv6] = "--enable-ipv6,--disable-ipv6,"
PACKAGECONFIG[krb5] = "--with-gssapi,--without-gssapi,krb5"
PACKAGECONFIG[ldap] = "--enable-ldap,--disable-ldap,"
PACKAGECONFIG[ldaps] = "--enable-ldaps,--disable-ldaps,"
PACKAGECONFIG[libidn] = "--with-libidn2,--without-libidn2,libidn2"
PACKAGECONFIG[libssh2] = "--with-libssh2,--without-libssh2,libssh2"
PACKAGECONFIG[nghttp2] = "--with-nghttp2,--without-nghttp2,nghttp2"
PACKAGECONFIG[pop3] = "--enable-pop3,--disable-pop3,"
PACKAGECONFIG[proxy] = "--enable-proxy,--disable-proxy,"
PACKAGECONFIG[rtmpdump] = "--with-librtmp,--without-librtmp,rtmpdump"
PACKAGECONFIG[rtsp] = "--enable-rtsp,--disable-rtsp,"
PACKAGECONFIG[smb] = "--enable-smb,--disable-smb,"
PACKAGECONFIG[smtp] = "--enable-smtp,--disable-smtp,"
PACKAGECONFIG[ssl] = "--with-ssl --with-random=/dev/urandom,--without-ssl,openssl"
PACKAGECONFIG[telnet] = "--enable-telnet,--disable-telnet,"
PACKAGECONFIG[tftp] = "--enable-tftp,--disable-tftp,"
PACKAGECONFIG[threaded-resolver] = "--enable-threaded-resolver,--disable-threaded-resolver"
PACKAGECONFIG[zlib] = "--with-zlib=${STAGING_LIBDIR}/../,--without-zlib,zlib"

EXTRA_OECONF = " \
    --enable-crypto-auth \
    --with-ca-bundle=${sysconfdir}/ssl/certs/ca-certificates.crt \
    --without-libmetalink \
    --without-libpsl \
"

do_debian_patch_prepend() {
	sed -i -e '/^90_gnutls.patch/d' -e '/^99_nss.patch/d' \
	    ${DEBIAN_QUILT_PATCHES}/series
}

do_install_append_class-target() {
	# cleanup buildpaths from curl-config
	sed -i \
	    -e 's,--sysroot=${STAGING_DIR_TARGET},,g' \
	    -e 's,--with-libtool-sysroot=${STAGING_DIR_TARGET},,g' \
	    -e 's|${DEBUG_PREFIX_MAP}||g' \
	    ${D}${bindir}/curl-config
}

do_compile_ptest() {
        oe_runmake -C ${B}/tests
}

do_install_ptest() {
        cat  ${WORKDIR}/disable-tests >> ${S}/tests/data/DISABLED
        rm -f ${B}/tests/configurehelp.pm
        cp -rf ${B}/tests ${D}${PTEST_PATH}
        rm -f ${D}${PTEST_PATH}/tests/libtest/.libs/libhostname.la
        rm -f ${D}${PTEST_PATH}/tests/libtest/libhostname.la
        mv ${D}${PTEST_PATH}/tests/libtest/.libs/* ${D}${PTEST_PATH}/tests/libtest/
        mv ${D}${PTEST_PATH}/tests/libtest/libhostname.so ${D}${PTEST_PATH}/tests/libtest/.libs/
        cp -rf ${S}/tests ${D}${PTEST_PATH}
        find ${D}${PTEST_PATH}/ -type f -name Makefile.am -o -name Makefile.in -o -name Makefile -delete
        install -d ${D}${PTEST_PATH}/src
        ln -sf ${bindir}/curl   ${D}${PTEST_PATH}/src/curl
        cp -rf ${D}${bindir}/curl-config ${D}${PTEST_PATH}
}

RDEPENDS_${PN}-ptest += "bash perl-modules"
RDEPENDS_${PN}-ptest_append_libc-glibc = " locale-base-en-us"

PACKAGES =+ "lib${BPN}"

FILES_lib${BPN} = "${libdir}/lib*.so.*"
RRECOMMENDS_lib${BPN} += "ca-certificates"

FILES_${PN} += "${datadir}/zsh"

BBCLASSEXTEND = "native nativesdk"
