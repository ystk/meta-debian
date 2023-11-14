SUMMARY = "Support for reading various archive formats"
DESCRIPTION = "C library and command-line tools for reading and writing tar, cpio, zip, ISO, and other archive formats"
HOMEPAGE = "http://www.libarchive.org/"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=ed99aca006bc346974bb745a35336425"

inherit debian-package
require recipes-debian/sources/libarchive.inc
FILESPATH_append = ":${COREBASE}/meta/recipes-extended/libarchive/libarchive"

DEPENDS = "e2fsprogs-native"

PACKAGECONFIG ?= "zlib bz2 xz lzo"

PACKAGECONFIG_append_class-target = "\
	libxml2 \
	${@bb.utils.filter('DISTRO_FEATURES', 'acl xattr', d)} \
"

DEPENDS_BZIP2 = "bzip2-replacement-native"
DEPENDS_BZIP2_class-target = "bzip2"

PACKAGECONFIG[acl] = "--enable-acl,--disable-acl,acl,"
PACKAGECONFIG[xattr] = "--enable-xattr,--disable-xattr,attr,"
PACKAGECONFIG[zlib] = "--with-zlib,--without-zlib,zlib,"
PACKAGECONFIG[bz2] = "--with-bz2lib,--without-bz2lib,${DEPENDS_BZIP2},"
PACKAGECONFIG[xz] = "--with-lzma,--without-lzma,xz,"
PACKAGECONFIG[openssl] = "--with-openssl,--without-openssl,openssl,"
PACKAGECONFIG[libxml2] = "--with-xml2,--without-xml2,libxml2,"
PACKAGECONFIG[expat] = "--with-expat,--without-expat,expat,"
PACKAGECONFIG[lzo] = "--with-lzo2,--without-lzo2,lzo,"
PACKAGECONFIG[nettle] = "--with-nettle,--without-nettle,nettle,"
PACKAGECONFIG[lz4] = "--with-lz4,--without-lz4,lz4,"

EXTRA_OECONF += "--enable-largefile"

SRC_URI += " \
           file://non-recursive-extract-and-list.patch \
           file://bug1066.patch \
"

# upstream-wontfix: upstream has documented that reported function is not thread-safe
CVE_CHECK_WHITELIST += " CVE-2023-30571"

inherit autotools update-alternatives pkgconfig

CPPFLAGS += "-I${WORKDIR}/extra-includes"

do_configure[cleandirs] += "${WORKDIR}/extra-includes"
do_configure_prepend() {
	# We just need the headers for some type constants, so no need to
	# build all of e2fsprogs for the target
	cp -R ${STAGING_INCDIR_NATIVE}/ext2fs ${WORKDIR}/extra-includes/
}

ALTERNATIVE_PRIORITY = "80"

PACKAGES =+ "bsdtar"
FILES_bsdtar = "${bindir}/bsdtar"

ALTERNATIVE_bsdtar = "tar"
ALTERNATIVE_LINK_NAME[tar] = "${base_bindir}/tar"
ALTERNATIVE_TARGET[tar] = "${bindir}/bsdtar"

PACKAGES =+ "bsdcpio"
FILES_bsdcpio = "${bindir}/bsdcpio"

ALTERNATIVE_bsdcpio = "cpio"
ALTERNATIVE_LINK_NAME[cpio] = "${base_bindir}/cpio"
ALTERNATIVE_TARGET[cpio] = "${bindir}/bsdcpio"

BBCLASSEXTEND = "native nativesdk"
