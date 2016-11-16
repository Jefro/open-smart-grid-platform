Feature: Device installation
  As a grid operator
  I want to be able to perform DeviceInstallation operations on a device
  In order to ...

  Scenario Outline: Adding a device
    When receiving an add device request
      | DeviceUid               | <DeviceUid>             |
      | DeviceIdentification    | <DeviceIdentification>  |
      | Alias                   | <Alias>                 |
      | Owner                   | <Owner>                 |
      | ContainerPostalCode     | <ContainerPostalCode>   |
      | ContainerCity           | <ContainerCity>         |
      | ContainerStreet         | <ContainerStreet>       |
      | ContainerNumber         | <ContainerNumber>       |
      | ContainerMunicipality   | <ContainerMunicipality> |
      | GpsLatitude             | <GpsLatitude>           |
      | GpsLongitude            | <GpsLongitude>          |
      | Activated               | <Activated>             |
      | HasSchedule             | <HasSchedule>           |
      | PublicKeyPresent        | <PublicKeyPresent>      |
      | DeviceModelManufacturer | <Manufacturer>          |
      | DeviceModelModelCode    | <ModelCode>             |
      | DeviceModelDescription  | <Description>           |
      | DeviceModelMetered      | <Metered>               |
    Then the add device response is successfull
    And the device exists
      | DeviceIdentification       | <DeviceIdentification>  |
      | Alias                      | <Alias>                 |
      | OrganisationIdentification | <Owner>                 |
      | ContainerPostalCode        | <ContainerPostalCode>   |
      | ContainerCity              | <ContainerCity>         |
      | ContainerStreet            | <ContainerStreet>       |
      | ContainerNumber            | <ContainerNumber>       |
      | ContainerMunicipality      | <ContainerMunicipality> |
      | GpsLatitude                | <GpsLatitude>           |
      | GpsLongitude               | <GpsLongitude>          |
#      | Activated                  | <Activated>             |
      | HasSchedule                | <HasSchedule>           |
      | PublicKeyPresent           | <PublicKeyPresent>      |
#      | DeviceModel                | <ModelCode>             |

    Examples: 
      | DeviceUid  | DeviceIdentification | Alias       | Owner    | ContainerPostalCode | ContainerCity | ContainerStreet | ContainerNumber | ContainerMunicipality | GpsLatitude | GpsLongitude | Activated | HasSchedule | PublicKeyPresent | Manufacturer | ModelCode  | Description | Metered |
      | 1234567890 | TEST1024000000001    | Test device | test-org | 1234AA              | Maastricht    | Stationsstraat  |              12 |                       |           0 |            0 | true      | false       | false            | Test         | Test Model | Test        | true    |

  Scenario Outline: Adding a device which already exists
    Given a device
      | DeviceIdentification | <DeviceIdentification> |
    When receiving an add device request
      | DeviceIdentification | <DeviceIdentification> |
    Then the add device response contains
      | FaultCode      | SOAP-ENV:Server                                                   |
      | FaultString    | EXISTING_DEVICE                                                   |
      | FaultType      | FunctionalFault                                                   |
      | Code           |                                                               204 |
      | Message        | EXISTING_DEVICE                                                   |
      | Component      | WS_CORE                                                           |
      | InnerException | com.alliander.osgp.domain.core.exceptions.ExistingEntityException |
      | InnerMessage   | Device with id <DeviceIdentification> already exists.             |
		
		Examples:
			| DeviceIdentification |
			| TEST1024000000001    |
  #Scenario: Updating a device
  #Given a device
  #| DeviceIdentification | TEST1024000000001 |
  #| Alias                | BeforeTest        |
  #When receiving an update device request
  #| DeviceIdentification | TEST1024000000001 |
  #| Alias                | AfterTest         |
  #Then the update device response is successfull
  #And the device exists
  #| DeviceIdentification | TEST1024000000001 |
  #| Alias                | AfterTest         |
  Scenario Outline: Updating a non existing device
    When receiving an update device request
      | DeviceIdentification | <DeviceIdentification> |
    Then the update device response contains
      | FaultCode      | SOAP-ENV:Server                                                  |
      | FaultString    | UNKNOWN_DEVICE                                                   |
      | FaultType      | FunctionalFault                                                  |
      | Code           |                                                              201 |
      | Message        | UNKNOWN_DEVICE                                                   |
      | Component      | WS_CORE                                                          |
      | InnerException | com.alliander.osgp.domain.core.exceptions.UnknownEntityException |
      | InnerMessage   | Device with id "<DeviceIdentification>" could not be found.      |
		
		Examples:
			| DeviceIdentification |
			| TEST1024000000001    |
			
### Converted Fitnesse tests to Cucumber ###
	# RemoveDevice scenario's
	Scenario Outline: Remove A Device
		Given a device
			| DeviceIdentification | <DeviceIdentification> |
		And the device exists
		When receiving a remove device request
		Then the remove device response contains
			| Result | <Result> |
		And the device should be removed
		
		Examples:
			| DeviceIdentification | Result |
			| TEST1024000000001    | OK     |

	# FindRecentDevices scenario's
	Scenario Outline: Find recent devices
		Given a device
      | DeviceIdentification | <DeviceIdentification> |
    When receiving a find recent devices request
    	| DeviceIdentification | <DeviceIdentification> |
    Then the find recent devices response contains
    	| DeviceIdentification | <DeviceIdentification> |
    	| Owner                | <Owner>                |
    
    Examples:
    	| DeviceIdentification | Owner    |
    	| TEST1024000000001    | test-org |
    	
  Scenario Outline: Find recent devices without owner
		Given a device
      | DeviceIdentification | <DeviceIdentification> |
    When receiving a find recent devices request
    	| DeviceIdentification | <DeviceIdentification> |
    Then the find recent devices response contains
    	| FaultCode      | SOAP-ENV:Server                                                  |
      | FaultString    | <FaultString>                                                    |
      | FaultType      | FunctionalFault                                                  |
      | Code           |                                                              201 |
      | Message        | <Message>                                                        |
      | Component      | WS_CORE                                                          |
      | InnerException | com.alliander.osgp.domain.core.exceptions.UnknownEntityException |
      | InnerMessage   | <InnerMessage>                                                   |
		
    
    Examples:
    	| DeviceIdentification | FaultString    | Message        | InnerMessage                                         |
    	| TEST1024000000001    | EMPTY_OWNER    | EMPTY_OWNER    | Can not find devices without 'Owner' parameter.      |
    	| TEST1024000000001    | UNKNOWN_DEVICE | UNKNOWN_DEVICE | Can not find devices with unknown 'Owner' parameter. |
  
  # RegisterDevices scenario's
	Scenario Outline: A Device Performs First Time Registration
		Given a not registered device
			| DeviceIdentification | <DeviceIdentification> |
		And the device returns a register device response over OSLP
		When receiving a register device request
		Then the register device response contains
			| DeviceUid            | <DeviceUid>            |
      | DeviceIdentification | <DeviceIdentification> |
      | DeviceType           | <DeviceType>           |
      | GpsLatitude          | <GpsLatitude>          |
      | GpsLongitude         | <GpsLongitude>         |
      | CurrentTime          | <CurrentTime>          |
      | TimeZone             | <TimeZone>             |
      
    Examples:
    	| DeviceUid  | DeviceIdentification | DeviceType | GpsLatitude | GpsLongitude | CurrentTime | TimeZone |
    	| 1234567890 | TEST1024000000001    |            |           0 |            0 |             |          |
    	
	Scenario Outline: A Device Performs First Time Registration
		Given a not registered device
			| DeviceUid            | <DeviceUid>            |
      | DeviceIdentification | <DeviceIdentification> |
      | DeviceType           | <DeviceType>           |
      | GpsLatitude          | <GpsLatitude>          |
      | GpsLongitude         | <GpsLongitude>         |
      | NetworkAddress       | <NetworkAddress>       |
      | CurrentTime          | <CurrentTime>          |
      | TimeZone             | <TimeZone>             |
		And the device returns a register device response over OSLP
			| Result | <Result> |
		When receiving a register device request
		Then the register device response contains
			| FaultCode      | SOAP-ENV:Server                                                   |
      | FaultString    | NETWORK_IN_USE                                                    |
      | FaultType      | FunctionalFault                                                   |
      | Code           |                                                               204 |
      | Message        | NETWORK_IN_USE                                                    |
      | Component      | WS_CORE                                                           |
      | InnerException | com.alliander.osgp.domain.core.exceptions.ExistingEntityException |
      | InnerMessage   | Network address <NetworkAddress> already used by another device.  |
      
    Examples:
    	| DeviceUid  | DeviceIdentification | DeviceType | GpsLatitude | GpsLongitude | NetworkAddress | CurrentTime | TimeZone | Result |
    	| 1234567890 | TEST1024000000001    |            |           0 |            0 | 0.0.0.0        |             |          | OK     |
    	
	# Start/Stop Device scenario's
	Scenario Outline: Start/Stop Device
		Given a device
			| DeviceIdentification | <DeviceIdentification> |
			| Status               | <Status>               |
		And the device returns a "<Option>" device response over OSLP
			| Result | <Result> |
		When receiving a "<Option>" device request
		Then the get status async response contains
      | DeviceIdentification | <DeviceIdentification> |
    And a "<Option>" device OSLP message is sent to device "<DeviceIdentification>"
		And the platform buffers a "<Option>" device response message for device "<DeviceIdentification>"
		
		Examples:
			| DeviceIdentification | Status | Option | Result |
			| TEST1024000000001    | active | start  | OK     |
			| TEST1024000000001    | active | stop   | OK     |