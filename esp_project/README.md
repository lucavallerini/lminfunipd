# Embedded Systems Programming (Programmazione di sistemi embedded)

# Details
+ Name of the project: Multiple Exposure HDR Imaging
+ Name of the group: HDRESP
+ Components of the group: Manuele Panozzo, Andrea Tonon, Luca Vallerini
+ Testing device: LGE Nexus 5 with Android Marshmallow 6.0.1

# App description
The proposed application can generate HDR photos with tone mapping applied. Two 
algorithms are proposed for the HDR (Debevec and Robertson) and four for the tone
mapping (Drago, Durand, Mantiuk and Reinhard).

The HDR can be generated either from photos shot by the device camera (only for 
device were Camera2 API are supported), aka _camera mode_, or from photos selected
from the device memory, aka _gallery mode_.

At the startup of the app, if the device supports Camera2 API, _camera mode_ is 
loaded, otherwise _gallery mode_ will be loaded. For those device that supports 
both _camera mode_ and _gallery mode_, it is possibile to switch from one mode to
the other.

From the UI of both modes it is possible to access the app settings. In the settings
is possible to choose:
+ shot resolution;
+ exposure step;
+ number of photos to shoot;
+ HDR algorithm;
+ tone mapping algorithm;
+ to align (or not) the photos;
+ to save (or not) intermediate photos or only the final one.

Settings related to the camera are available only to those devices where Camera2 API are
supported.

At the end of the sequence shotting or after the selection of the photos from the device
memory, the elaboration of the final image will be started: due to the time of the operation
(on average 30-60 seconds long) we chose to not let the user interact with the app and a dialog
with a progress bar will be displayed. At the end, if everything went well, the final image will
be showed and the user can interact with the app again.
