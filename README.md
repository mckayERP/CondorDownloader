# Introduction
Condor Downloader is a simple utility aimed at pilots using the Condor Soaring simulator.  It automates the process of downloading a flight track and best performances from the Condor.Club website, saving the user from the monotonous task of doing the same by hand.
# Limitations
Condor Downloader is a stand-alone JavaFX application running on Windows only.  It requires the FireFox browser is installed. 
# Installation
The utility will available as a zip file.  The zip file can be extracted to any directory.  The application is started by running the "Ccondor Downloader\bin\condorDownloader.bat" batch file.
# Instructions for Use
## Settings
Before use, the user should set the settings which include the user's Condor.Club user name and password, the path to the FireFox executable (in case the utility can't find it) and the paths to the directories where the downloaded files should be saved.
## Task Code
The task code is the Task Network ID used by Condor.Club to identify the task.  This code can be found on the Task Description page and should look like  "Network id: 	ABCEDF".  The code will have five or six capital letters.  Use this code in the Task Code field of the utility.

You can chose to download the flight track and one or more of the best performances (results). Only the top results are downloaded. If the task was used in a competition, you can download those tracks by selected the appropriate checkbox on the utility.  The tracks are downloaded one at a time and this can be time consuming.  
If the flight track already exists in the ghost folder, it won't be downloaded again.  The number of downloads is unlimited but 5 to 10 is reasonable.  If there aren't as many results as desired, the utility will download all the results available. 

The best performances are saved a ghost flight tracks in a directory set in the Settings.  If you select the "Copy ghosts to flight track folder", the ghost files will also be copied to the that folder so they are ready to be used in the simulator.  

Under the File menu is a selection to "Clean up flight track folder." The menu selection will delete all the ghost flight tracks from the flight track folder.

