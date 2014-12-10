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
PROJECT_DIR=$(pwd)

# translations directory
TRANSLATIONS_DIR="${PROJECT_DIR}/backend/po"

if [ ! -d "$TRANSLATIONS_DIR" ]; then
	echo "Could not find translactions directory at: ${TRANSLATIONS_DIR}"
	exit
fi

# url to fetch translations from
FETCH_URL="https://api.crowdin.com/api/project/${CROWDIN_PROJECT_ID}/download/all.zip?key=${CROWDIN_API_KEY}"

TRANSLATION_TEMP_DIR=$(mktemp -d ${PROJECT_DIR}/${CROWDIN_PROJECT_ID}.XXXXXXX)
trap "rm -rf ${TRANSLATION_TEMP_DIR}" EXIT

echo "Downloading translations to ${TRANSLATION_TEMP_DIR}/all.zip"
curl -L -o "${TRANSLATION_TEMP_DIR}/all.zip" "$FETCH_URL"

echo "Copying downloaded translations to ${TRANSLATIONS_DIR}"
cd "${TRANSLATION_TEMP_DIR}"
unzip -q all.zip -d extracted
find extracted -name *.po -exec cp "{}" "${TRANSLATIONS_DIR}" \;
