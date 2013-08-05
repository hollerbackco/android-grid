Android Codebase Notes

Libraries Required for JavaCV

Compiling NDK Notes
- Note the exiting libraries in armeabi before executing an ndk build on jni

Backburner Android:
- Implement FragmentManagerUtil to manage the backstack of fragments
- Remove use of in memory temp storage
- DataModelManager is an attempt to tie together all call aspects
- S3UploadParams is an attmpted bucket abstraction for interfacing w/ the s3 library.
- There is a callback for progress that is implemented, considering broadcast local messages for cleaner in app models
- Finishing the background service implementation


Quirks:
- when images are cached, lrucache will find appended -thumb.png link and omit all params afterwards are part of the key

Libraries:
- ActiveAndroid
- JakeWharton/LRUDiskCache
- LoopJ/AsyncHttpLibrary

Fix for FFMPEG SegFaults
Run FFMPEG in a new process by intent calling to a service (due this for less important thread)