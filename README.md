# CameraFocusPermission

# Implementation

Step 1. Add the JitPack repository to your build file


Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
Step 2. Add the dependency

    dependencies {
	        implementation 'com.github.Darthkiler:CameraFocusPermissions:1.0.14'
	}
	
	
# Usage

    CameraPerformer cameraPerformer;
    
Activity

    cameraPerformer = new CameraPerformer(this, this, this, null)
    
Fragment

    cameraPerformer = new CameraPerformer(this, this, this, this)
    
Required

    .setCamera(findViewById(R.id.camera)) //id for cameraView
    .setFlashButton(findViewById(R.id.camera_fragment_flash)) //id for imageView for torch 
    .setTakePicture(findViewById(R.id.camera_fragment_take_photo)) //id for imageView for take picture
    .build(); //create final object, required and optional methods
    
    
onActivityResult and onRequestPermissionsResult

    cameraPerformer.onActivityResult(requestCode, resultCode, data); //call this in activity or fragment(if you use fragment lifecycle) result
    
    cameraPerformer.onRequestPermissionsResult(requestCode, permissions, grantResults); //call this in activity or fragment(if you use fragment lifecycle) request permissions
    
Optional

    .setShowErrorAlert(true) //show error alert
    .setAlertCameraError(findViewById(R.id.alert_camera_error)) //alert error layout id
    .setAlertCameraErrorTitle(R.id.alertCameraErrorTitle) //alert error layout title id
    .setAlertCameraErrorBody(R.id.alertCameraErrorBody) //alert error layout body id
    .setAlertCameraErrorPositive(R.id.camera_fragment_error_alert_refresh) //alert error OK button
    
    
    
    .setBrowseImageView(findViewById(R.id.camera_fragment_browse)) //id for browse button
    
    
    .setCameraFocusViewResource(R.drawable.ic_launcher_background) //set drawable for focus view
    
    
    
    .setPermissionUtils(new AlertUtils.PostPermissionUtils("error","need permission","settings","cancel")) //create post permision alert with custom text
    
    
    .setInactiveAlphaValue(0.8f) //set alpha value for inactive flash view
    
    
    .setTakeSnapshot(true) //allows you to take snapshots instead of photos
    
    
    .setCameraResultCallBack(new CameraResultCallBack() {}) //set callback for all key events


