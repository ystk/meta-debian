#!/bin/bash
#
# Common functions for other scripts.

TEST_DISTROS=${TEST_DISTROS:-deby-tiny}
TEST_MACHINES=${TEST_MACHINES:-qemux86}
BUILDDIR=${BUILDDIR:-build}
VERBOSE=${VERBOSE:-0}

declare -A REQUIRED_DISTRO_FEATURES

RED='\e[91m'
BLD='\e[1m'
BLD_RED='\e[1;91m'
RST='\e[0m'

function error {
	msg="$1"
	echo -e "${BLD_RED}ERROR${RST}${RED}: ${msg}${RST}"
}

function note {
	msg="$1"
	echo -e "${BLD}NOTE${RST}: ${msg}"
}

# Run bitbake and write log to file.
# Params:
#   $1: recipe name
#   $2: log file
function build {
	if [[ $# -lt 1 ]]; then
		error "$FUNCNAME $*: missing argument."
		exit 1
	fi

	recipe=$1
	logfile=${2:-/dev/null}
	mkdir -p `dirname $logfile`

	ret=0
	if [ "$VERBOSE" = "1" ]; then
		set -o pipefail
		bitbake $recipe 2>&1 | tee $logfile
		ret=$?
		set +o pipefail
	else
		bitbake $recipe &> $logfile
		ret=$?
	fi

	return $ret
}

# Update variable in file.
# Params:
#   $1: variable which will be updated. Eg: IMAGE_INSTALL_append
#   $2: value which will be set. Eg: " bzip2 zlib"
#   $3: config file which will be modified. Eg: conf/local.conf
#   $4: Optional. Type of set_var: overwrite (default) or append.
function _set_var {
	if [[ $# -lt 3 ]]; then
		error "$FUNCNAME $*: missing argument."
		exit 1
	fi

	key="$1"
	val="$2"
	file="$3"
	type="$4"

	if grep -q "^$key\s*?*=" $file 2> /dev/null; then
		if [ "$type" = "append" ]; then
			sed -i -e "s#\(^$key\s*?*=.*\)\"#\1 $val\"#" $file
		else
			sed -i -e "s#\(^$key\s*?*=\).*#\1 \"$val\"#" $file
		fi
	else
		echo "$key = \"$val\"" >> $file
	fi
}
function set_var {
	_set_var "$1" "$2" "$3"
}
function append_var {
	_set_var "$1" "$2" "$3" append
}

# Get HEAD id in short of the first 7 character
# Params:
#   $1: Directory you want to get HEAD
function git_head {
	git -C $1 rev-parse HEAD | head -c 7
}

# Set up a build directory.
# No params.
function setup_builddir {
	note "Setup build directory."
	cd $WORKDIR
	rm -rf $BUILDDIR/conf
	export TEMPLATECONF=meta-debian/conf
	source ./poky/oe-init-build-env $BUILDDIR

	if which gitproxy &> /dev/null; then
		set_var "HOSTTOOLS_append" " gitproxy" conf/local.conf
	fi

	# Configurations for u-boot
	set_var "UBOOT_MACHINE_qemux86" "qemu-x86_defconfig" conf/local.conf
	set_var "UBOOT_MACHINE_qemux86-64" "qemu-x86_64_defconfig" conf/local.conf
	set_var "UBOOT_MACHINE_qemumips" "qemu_mips_defconfig" conf/local.conf
	set_var "UBOOT_MACHINE_qemuppc" "qemu-ppce500_defconfig" conf/local.conf
	set_var "UBOOT_MACHINE_qemuarm" "qemu_arm_defconfig" conf/local.conf
	set_var "UBOOT_MACHINE_qemuarm64" "qemu_arm64_defconfig" conf/local.conf

	# Some recipes require specific DISTRO_FEATURES to build
	set_var "DISTRO_FEATURES_append" " $TEST_DISTRO_FEATURES \${DISTRO_FEATURES_TMP}" conf/local.conf
	set_var "DISTRO_FEATURES_TMP" "" conf/local.conf
}

# Get name of all recipes and ptest packages.
# No params.
function get_all_packages {
	BTEST_PACKAGES=""
	PTEST_PACKAGES=""

	recipes=`find $THISDIR/.. -name *.bb`
	for recipe in $recipes; do
		recipe_env="bb_e.env"
		bitbake -e -b $recipe > $recipe_env

		# Get the final PN
		pn=`grep "^PN=" $recipe_env | cut -d\" -f2`

		# Check if recipe is buildable (task do_build exists)
		if ! grep -q "^_task_deps=\"{'tasks': \[[^]]*do_build" $recipe_env; then
			continue
		fi

		BTEST_PACKAGES="$BTEST_PACKAGES $pn"

		# Check if ptest available
		ptest_enabled=`grep "^PTEST_ENABLED=" $recipe_env | cut -d\" -f2`
		if [ "$ptest_enabled" = "1" ]; then
			PTEST_PACKAGES="$PTEST_PACKAGES $pn"
		fi

		# Get REQUIRED_DISTRO_FEATURES
		required_distro_features=`grep "^REQUIRED_DISTRO_FEATURES=" $recipe_env | cut -d\" -f2`
		REQUIRED_DISTRO_FEATURES[$pn]=required_distro_features

		# Get BBCLASSEXTEND
		bbclassextend=`grep "^BBCLASSEXTEND=" $recipe_env | cut -d\" -f2`
		for variant in $bbclassextend; do
			if [ "$variant" = "native" ] || [ "$variant" = "cross" ]; then
				BTEST_PACKAGES="$BTEST_PACKAGES ${pn}-$variant"
				REQUIRED_DISTRO_FEATURES[${pn}-$variant]=required_distro_features
			else
				BTEST_PACKAGES="$BTEST_PACKAGES ${variant}-$pn"
				REQUIRED_DISTRO_FEATURES[${variant}-$pn]=required_distro_features
			fi
		done

		rm -f $recipe_env
	done
}

function setup_security_update_repository() {
	set_var "DEBIAN_SECURITY_UPDATE_MIRROR" "http://security.debian.org/debian-security/pool/updates" conf/local.conf
	set_var "DEBIAN_ELTS_SECURITY_UPDATE_MIRROR" "http://deb.freexian.com/extended-lts/pool" conf/local.conf
}
