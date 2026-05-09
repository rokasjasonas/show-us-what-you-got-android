"""
Upload an AAB to Google Play Store using the Publishing API.

Usage:
  python upload_to_play_store.py \
    --package-name com.example.app \
    --aab path/to/app.aab \
    --track internal \
    --key path/to/service-account-key.json
"""

import argparse
import sys

from googleapiclient.discovery import build
from oauth2client.service_account import ServiceAccountCredentials

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]


def main():
    parser = argparse.ArgumentParser(description="Upload AAB to Google Play Store")
    parser.add_argument("--package-name", required=True, help="Application package name")
    parser.add_argument("--aab", required=True, help="Path to the AAB file")
    parser.add_argument("--track", default="internal", help="Play Store track (internal/alpha/beta/production)")
    parser.add_argument("--key", required=True, help="Path to service account JSON key")
    args = parser.parse_args()

    # Authenticate
    credentials = ServiceAccountCredentials.from_json_keyfile_name(args.key, scopes=SCOPES)
    service = build("androidpublisher", "v3", credentials=credentials)

    package_name = args.package_name

    try:
        # Create a new edit
        edit = service.edits().insert(body={}, packageName=package_name).execute()
        edit_id = edit["id"]
        print(f"  Created edit: {edit_id}")

        # Upload the AAB
        print(f"  Uploading {args.aab}...")
        bundle = service.edits().bundles().upload(
            editId=edit_id,
            packageName=package_name,
            media_body=args.aab,
            media_mime_type="application/octet-stream",
        ).execute()
        version_code = bundle["versionCode"]
        print(f"  Uploaded version code: {version_code}")

        # Assign to track
        service.edits().tracks().update(
            editId=edit_id,
            packageName=package_name,
            track=args.track,
            body={
                "track": args.track,
                "releases": [
                    {
                        "versionCodes": [str(version_code)],
                        "status": "completed",
                    }
                ],
            },
        ).execute()
        print(f"  Assigned to track: {args.track}")

        # Commit the edit
        service.edits().commit(editId=edit_id, packageName=package_name).execute()
        print(f"  Edit committed successfully!")

    except Exception as e:
        print(f"  ❌ Upload failed: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()

