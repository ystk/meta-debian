#
# base recipe: meta/recipes-core/glibc/glibc_2.28.bb
# base branch: master
# base commit: 6f2ef620d90ab39870bb6c02183249e0e1045aeb
#

require recipes-core/glibc/glibc.inc

inherit debian-package
require recipes-debian/sources/glibc.inc
BPN = "glibc"

LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = " \
file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c \
file://LICENSES;md5=cfc0ed77a9f62fa62eded042ebe31d72 \
"

DEPENDS += "gperf-native bison-native"
PROVIDES += "virtual/crypt"

FILESPATH_append = ":${COREBASE}/meta/recipes-core/glibc/glibc"

# Ignore patches 0018, 0019 and 0020 because Debian already has similar patches.
SRC_URI += " \
           file://etc/ld.so.conf \
           file://generate-supported.mk \
           file://makedbs.sh \
           \
           ${NATIVESDKFIXES} \
           file://0006-fsl-e500-e5500-e6500-603e-fsqrt-implementation.patch \
           file://0007-readlib-Add-OECORE_KNOWN_INTERPRETER_NAMES-to-known-.patch \
           file://0008-ppc-sqrt-Fix-undefined-reference-to-__sqrt_finite.patch \
           file://0009-__ieee754_sqrt-f-are-now-inline-functions-and-call-o.patch \
           file://0010-Quote-from-bug-1443-which-explains-what-the-patch-do.patch \
           file://0011-eglibc-run-libm-err-tab.pl-with-specific-dirs-in-S.patch \
           file://0012-__ieee754_sqrt-f-are-now-inline-functions-and-call-o.patch \
           file://0013-sysdeps-gnu-configure.ac-handle-correctly-libc_cv_ro.patch \
           file://0015-yes-within-the-path-sets-wrong-config-variables.patch \
           file://0016-timezone-re-written-tzselect-as-posix-sh.patch \
           file://0017-Remove-bash-dependency-for-nscd-init-script.patch \
           file://0018-eglibc-Cross-building-and-testing-instructions.patch \
           file://0022-eglibc-Forward-port-cross-locale-generation-support.patch \
           file://0023-Define-DUMMY_LOCALE_T-if-not-defined.patch \
           file://0024-localedef-add-to-archive-uses-a-hard-coded-locale-pa.patch \
           file://0025-elf-dl-deps.c-Make-_dl_build_local_scope-breadth-fir.patch \
           file://0025-locale-fix-hard-coded-reference-to-gcc-E.patch \
           file://0028-bits-siginfo-consts.h-enum-definition-for-TRAP_HWBKP.patch \
           file://0028-intl-Emit-no-lines-in-bison-generated-files.patch \
           file://0029-inject-file-assembly-directives.patch \
           file://0031-sysdeps-ieee754-prevent-maybe-uninitialized-errors-w.patch \
           file://0033-locale-prevent-maybe-uninitialized-errors-with-Os-BZ.patch \
           "

NATIVESDKFIXES ?= ""
NATIVESDKFIXES_class-nativesdk = "\
           file://0001-nativesdk-glibc-Look-for-host-system-ld.so.cache-as-.patch \
           file://0002-nativesdk-glibc-Fix-buffer-overrun-with-a-relocated-.patch \
           file://0003-nativesdk-glibc-Raise-the-size-of-arrays-containing-.patch \
           file://0004-nativesdk-glibc-Allow-64-bit-atomics-for-x86.patch \
           file://0005-nativesdk-glibc-Make-relocatable-install-for-locales.patch \
"

B = "${WORKDIR}/build-${TARGET_SYS}"

PACKAGES_DYNAMIC = ""

# the -isystem in bitbake.conf screws up glibc do_stage
BUILD_CPPFLAGS = "-I${STAGING_INCDIR_NATIVE}"
TARGET_CPPFLAGS = "-I${STAGING_DIR_TARGET}${includedir}"

GLIBC_BROKEN_LOCALES = ""

GLIBCPIE ??= ""

EXTRA_OECONF = "--enable-kernel=${OLDEST_KERNEL} \
                --disable-profile \
                --disable-debug --without-gd \
                --enable-clocale=gnu \
                --with-headers=${STAGING_INCDIR} \
                --without-selinux \
                --enable-obsolete-rpc \
                --enable-tunables \
                --enable-bind-now \
                --enable-stack-protector=strong \
                --enable-stackguard-randomization \
                --enable-nscd \
                ${@bb.utils.contains_any('SELECTED_OPTIMIZATION', '-O0 -Og', '--disable-werror', '', d)} \
                ${GLIBCPIE} \
                ${GLIBC_EXTRA_OECONF}"

EXTRA_OECONF += "${@get_libc_fpu_setting(bb, d)}"

do_patch_append() {
    bb.build.exec_func('do_fix_readlib_c', d)
}

do_fix_readlib_c () {
	sed -i -e 's#OECORE_KNOWN_INTERPRETER_NAMES#${EGLIBC_KNOWN_INTERPRETER_NAMES}#' ${S}/elf/readlib.c
}

do_configure () {
	echo "config:" >> ${S}/Makeconfig
	sed -i -e "s:manual::" ${S}/Makeconfig

	# override this function to avoid the autoconf/automake/aclocal/autoheader
	# calls for now
	# don't pass CPPFLAGS into configure, since it upsets the kernel-headers
	# version check and doesn't really help with anything
	(cd ${S} && gnu-configize) || die "failure in running gnu-configize"
	find ${S} -name "configure" | xargs touch
	CPPFLAGS="" oe_runconf
}

do_compile () {
	# -Wl,-rpath-link <staging>/lib in LDFLAGS can cause breakage if another glibc is in staging
	unset LDFLAGS
	base_do_compile
	echo "Adjust ldd script"
	if [ -n "${RTLDLIST}" ]
	then
		prevrtld=`cat ${B}/elf/ldd | grep "^RTLDLIST=" | sed 's#^RTLDLIST="\?\([^"]*\)"\?$#\1#'`
		if [ "${prevrtld}" != "${RTLDLIST}" ]
		then
			sed -i ${B}/elf/ldd -e "s#^RTLDLIST=.*\$#RTLDLIST=\"${prevrtld} ${RTLDLIST}\"#"
		fi
	fi
}

do_install_append() {
	# TODO: Should disable build/install these files by configurations.
	# Poky has split rpc and libnsl to other recipes,
	# this is workaround to avoid conflict with libnsl2, quota.
	rm -f ${D}${includedir}/rpcsvc/yppasswd.*
	rm -f ${D}${includedir}/rpcsvc/rquota.*
	rm -f ${D}${libdir}/libnsl*

	install -m 0644 ${S}/debian/local/etc/nsswitch.conf ${D}${sysconfdir}/
}

require recipes-core/glibc/glibc-package.inc

# This is a backport of poky's 38fce3d2fd998a67604f9492ab3a571f963c5df3
RDEPENDS_${PN}-dev = "linux-libc-headers-dev"

stash_locale_sysroot_cleanup() {
        stash_locale_cleanup ${SYSROOT_DESTDIR}
        # We don't want to ship an empty /usr/share
        rmdir --ignore-fail-on-non-empty ${SYSROOT_DESTDIR}${datadir}
}
stash_locale_package_cleanup() {
        stash_locale_cleanup ${PKGD}
        # We don't want to ship an empty /usr/share
        rmdir --ignore-fail-on-non-empty ${PKGD}${datadir}
}

# for glibc-2.28
# bits/procfs-id.h bits/procfs.h bits/shmlba.h are not provided at glibc-2.28
do_install_armmultilib () {
        oe_multilib_header bits/endian.h bits/fcntl.h bits/fenv.h bits/fp-fast.h bits/hwcap.h bits/ipc.h bits/link.h bits/wordsize.h
        oe_multilib_header bits/local_lim.h bits/mman.h bits/msq.h bits/pthreadtypes.h bits/pthreadtypes-arch.h  bits/sem.h  bits/semaphore.h bits/setjmp.h
        oe_multilib_header bits/shm.h bits/sigstack.h bits/stat.h bits/statfs.h bits/typesizes.h

        oe_multilib_header fpu_control.h gnu/lib-names.h gnu/stubs.h ieee754.h

        oe_multilib_header sys/elf.h sys/procfs.h sys/ptrace.h sys/ucontext.h sys/user.h
}

FILES_${PN} += "${sysconfdir}/nsswitch.conf"
FILES_${PN} += "${base_libdir}/libcrypt*.so.* ${base_libdir}/libcrypt-*.so"

BBCLASSEXTEND = "nativesdk"

PTEST_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'ptest', '1', '0', d)}"
PTEST_ENABLED_class-native = ""
PTEST_ENABLED_class-nativesdk = ""
PTEST_ENABLED_class-cross-canadian = ""
PACKAGES += "${@bb.utils.contains('PTEST_ENABLED', '1', 'glibc-ptest', '', d)}"
ALLOW_EMPTY_glibc-ptest = "1"
RDEPENDS_glibc-ptest_class-target = "glibc-tests"
