import Foundation
import NetworkExtension

@objc(simplewificonnector) class simplewificonnector : CDVPlugin {
  @objc(connect:)
  func connect(_ command: CDVInvokedUrlCommand) {
    
    let ssid = command.arguments[0] as? String ?? "Error"
    let password = command.arguments[1] as? String ?? "Error"
    var pluginResult = CDVPluginResult(
status: CDVCommandStatus_ERROR,
messageAs: "Network unavailable or wrong password"
)
        let configuration = NEHotspotConfiguration.init(ssid: ssid, passphrase: password, isWEP: false)
        configuration.joinOnce = true

        NEHotspotConfigurationManager.shared.apply(configuration) { (error) in
            if error != nil {
                if error?.localizedDescription == "already associated."
                {
	                pluginResult = CDVPluginResult(
       	 		status: CDVCommandStatus_OK,
		        messageAs: ssid
      			)
                }
                else{
                    	pluginResult = CDVPluginResult(
      			status: CDVCommandStatus_ERROR,
			messageAs: "Network unavailable or wrong password"
    )
                }
            }
            else {
                pluginResult = CDVPluginResult(
       	 		status: CDVCommandStatus_OK,
		        messageAs: ssid
      		)
            }
        }

	self.commandDelegate!.send(
      		pluginResult,
      		callbackId: command.callbackId
    	) 
  }
}
