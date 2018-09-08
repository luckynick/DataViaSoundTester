netsh wlan connect name=%1
IF %ERRORLEVEL% NEQ 0 EXIT /B 1