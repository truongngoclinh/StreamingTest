## ~DEPRECATED~ moving out to new repo [Ezstreaming] (https://github.com/truongngoclinh/ezstreaming)
### Streaming mobile android over rtmp
### Function
* Streaming rear camera
* Streaming screen with front camera

### How it works
#### Screen recording
* Using MediaCodec as encoder
* Get H264 bitstream from encoder
* Encode to flv, packet rtpm and send to server

#### Rear camera
* Using camera deprecated api
* Using javacv: https://github.com/vanevery/JavaCV_0.3_stream_test
