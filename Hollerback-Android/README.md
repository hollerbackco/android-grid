Dependencies:

ActionBarSherlock
http://actionbarsherlock.com/

SlidingMenu
https://github.com/jfeinstein10/SlidingMenu

Android Async Http
http://loopj.com/android-async-http/

Step 1.
  Custom Camera
  - Across Devices
  - Save the video
  - Crop source video to square
  - Save some flag for that file to uploaded
    - verify file size/lenght? maybe
  - File Size Smashing

Step 2. 
  - Throw in http library
  - Write some helpers on to of that (copy that in)
  - Helpers for S3 (Throw that in)
  - Upload / check response
  - State check
  - POST (params/request)

Last Step.
  - Theming

Production Steps:

  allow front face and back facing camera option

  onAppStart/Background
  - checked if any queued video files to be sent

  Small database
  Save locally video file was taken, flag if successfully uploaded on client side

  Background Service
    - That will take care of uploads/downloads w/out app being open
    - Last as long as until user kills it

  Security
    - Do not keep important keys / access_ids located on the client package
      - Request these from an authenticated call ideally