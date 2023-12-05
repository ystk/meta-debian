#
# Base recipe: meta/recipes-core/ncurses/ncurses_6.1.bb
# Base branch: master
# Base commit: a5d1288804e517dee113cb9302149541f825d316
# 

require recipes-core/ncurses/ncurses.inc

inherit debian-package
require recipes-debian/sources/ncurses.inc
DEBIAN_UNPACK_DIR = "${WORKDIR}/${BPN}-${@d.getVar('PV', True).replace('+','-')}"

EXTRA_OECONF += "--with-abi-version=5"

SRC_URI += "file://run-ptest"

inherit ptest
EXTRA_OECONF += "${@bb.utils.contains('PTEST_ENABLED', '1', '--with-tests', '', d)}"

do_compile_ptest() {
    oe_runmake -C narrowc/test all
    ! ${ENABLE_WIDEC} || \
        oe_runmake -C widec/test all
}

do_install_ptest() {
    install -dm 755 ${D}${PTEST_PATH}/narrowc/
    install -m 755 ${B}/narrowc/test/test_setupterm ${D}${PTEST_PATH}/narrowc/
    if [ ${ENABLE_WIDEC} ]; then
        install -dm 755 ${D}${PTEST_PATH}/widec/
        install -m 755 ${B}/widec/test/test_setupterm ${D}${PTEST_PATH}/widec/
    fi

    # handle multilib
    sed -i s:@libdir@:${libdir}:g ${D}${PTEST_PATH}/run-ptest
}

RDEPENDS_${PN}-ptest += "${PN}-terminfo"
