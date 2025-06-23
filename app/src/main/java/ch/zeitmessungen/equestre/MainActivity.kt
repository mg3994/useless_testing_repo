name: Make APK and Push into release tab

on:
  push:
    branches:
      - '*'  # Trigger on pushes to all branches
  pull_request:
    branches:
      - '*'  # Trigger on pushes to all branches
  workflow_dispatch:  # Allows manual triggering of the workflow

jobs:
  build:
    runs-on: macos-latest
    permissions: write-all

    steps:
    - name: Checkout repository
      uses: actions/checkout@v4
      

    - name: "Build APK Project"
      run: |
       ./gradlew assembleRelease

    - name: Push into Releases Tab
      uses: ncipollo/release-action@v1
      with:
        artifacts: "app/build/outputs/apk/release/*"
        tag: v1.0.${{ github.run_number }}
        token: ${{ secrets.GITHUB_TOKEN}}