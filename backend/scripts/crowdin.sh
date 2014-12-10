#!/bin/bash

if [ "${CROWDIN_PROJECT_ID}" = "" ]; then
	echo "No Crowdin project id found in env var: CROWDIN_PROJECT_ID"
	exit 1
fi

if [ "${CROWDIN_API_KEY}" = "" ]; then
	echo "No Crowdin API key found in env var: CROWDIN_API_KEY"
	exit 1
fi

# directory the project is in
PROJECT_DIR="`pwd`"

# translations directory
TRANSLATIONS_DIR="${PROJECT_DIR}/backend/po"

if [ ! -d "$TRANSLATIONS_DIR" ]; then
	echo "Could not find translactions directory at: ${TRANSLATIONS_DIR}"
	exit
fi

# url to fetch translations from
FETCH_URL="https://api.crowdin.com/api/project/${CROWDIN_PROJECT_ID}/download/all.zip?key=${CROWDIN_API_KEY}"

TMPDIR="`mktemp -d -t ${CROWDIN_PROJECT_ID}-crowdin`"
trap "rm -rf $TMPDIR" EXIT

echo "Downloading translactions to ${TMPDIR}/all.zip"
curl -L -o "${TMPDIR}/all.zip" "$FETCH_URL" 

echo "Copying downloaded translations to ${TRANSLATIONS_DIR}"
cd "${TMPDIR}"
unzip -q all.zip -d extracted
find extracted -name *.po -exec cp "{}" "${TRANSLATIONS_DIR}" \;
