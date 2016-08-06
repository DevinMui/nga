/**
 * PPKController.h
 * P2PKit
 *
 * Copyright (c) 2016 by Uepaa AG, Zürich, Switzerland.
 * All rights reserved.
 *
 * We reserve all rights in this document and in the information contained therein.
 * Reproduction, use, transmission, dissemination or disclosure of this document and/or
 * the information contained herein to third parties in part or in whole by any means
 * is strictly prohibited, unless prior written permission is obtained from Uepaa AG.
 *
 */

#import <Foundation/Foundation.h>
#import "PPKPeer.h"

#define P2PKIT_VERSION @"1.3.6"

/*!
 *  Possible states of the P2P discovery engine.
 */
typedef NS_ENUM(NSInteger, PPKPeer2PeerDiscoveryState) {
    
    /*! P2P discovery is disabled.*/
    PPKPeer2PeerDiscoveryStopped,
    
    /*! P2P discovery is not supported on this device (e.g. BLE is not available).*/
    PPKPeer2PeerDiscoveryUnsupported,
    
    /*! P2P discovery is not able to run because of a missing user permission.*/
    PPKPeer2PeerDiscoveryUnauthorized,
    
    /*! P2P discovery is temporarily suspended. The discovery engine will try to restart as soon as possible (e.g. after the user did re-enable BLE).*/
    PPKPeer2PeerDiscoverySuspended,
    
    /*! P2P discovery is running: the device will discover other peers and will be discovered by other peers.*/
    PPKPeer2PeerDiscoveryRunning
};

/*!
 *  Possible states of the GEO discovery engine.
 */
typedef NS_ENUM(NSInteger, PPKGeoDiscoveryState) {
    
    /*! GEO discovery is disabled.*/
    PPKGeoDiscoveryStopped,
    
    /*! GEO discovery is temporarily suspended. The discovery engine will try to restart periodically (e.g. after internet connectivity has been re-established).*/
    PPKGeoDiscoverySuspended,
    
    /*! GEO discovery is running: the device will discover other peers and will be discovered by other peers.*/
    PPKGeoDiscoveryRunning
};

/*!
 *  Possible states of the Online messaging engine.
 */
typedef NS_ENUM(NSInteger, PPKOnlineMessagingState) {
    
    /*! Online messaging is disabled.*/
    PPKOnlineMessagingStopped,
    
    /*! Online messaging is temporarily suspended. The messaging engine will try to restart periodically (e.g. after internet connectivity has been re-established).*/
    PPKOnlineMessagingSuspended,
    
    /*! Online messaging is running: the device will be able to send and receive Online messages.*/
    PPKOnlineMessagingRunning
};

/*!
 *  P2PKit initialization error codes.
 */
typedef NS_ENUM(NSInteger, PPKErrorCode) {
    
    /*! P2PKit initialization failed due to an invalid App Key. Please configure your App Key in the <code><a href="https://p2pkit-console.uepaa.ch/">p2pkit console</a></code>.*/
    PPKErrorAppKeyInvalid,
    
    /*! P2PKit initialization failed due to an expired configuration key. Please obtain a new key.*/
    PPKErrorAppKeyExpired __deprecated_enum_msg("Use PPKErrorAppKeyInvalid instead."),
    
    /*! Server connection failed due to a server incompatibility. Please update to the most recent version of the framework.*/
    PPKErrorOnlineProtocolVersionNotSupported,
    
    /*! Server connection failed due to an invalid app configuration key. Please obtain a valid key.*/
    PPKErrorOnlineAppKeyInvalid __deprecated_enum_msg("Use PPKErrorAppKeyInvalid instead."),
    
    /*! Server connection failed due to an expired configuration key. Please obtain a new key.*/
    PPKErrorOnlineAppKeyExpired __deprecated_enum_msg("Use PPKErrorAppKeyInvalid instead."),
    
    /*! P2PKit initialization failed due to an invalid bundle ID. Please configure your bundle IDs in the <code><a href="https://p2pkit-console.uepaa.ch/">p2pkit console</a></code>.*/
    PPKErrorInvalidBundleId
};



#pragma mark - PPKControllerDelegate

/*!
 *  Delivers lifecycle events, discovery events and messaging events
 */
@protocol PPKControllerDelegate <NSObject>
@optional


/*!
 *  @name   Lifecycle
 */
#pragma mark Lifecycle

/*!
 *  @abstract     Indicates successful initialization of the P2PKit. You must not call other methods before this delegate method has been called.
 */
-(void)PPKControllerInitialized;

/*!
 *  @abstract       Indicates an error with P2PKit (e.g. invalid configuration or server incompatibility due to an outdated version).
 *
 *  @param error    error containing the appropriate <code> PPKErrorCode </code>
 *
 *  @see            <code> PPKErrorCode </code>
 */
-(void)PPKControllerFailedWithError:(NSError*)error;


/*!
 *  @name   P2P Discovery
 */
#pragma mark P2P Discovery

/*!
 *  @abstract       Indicates a state change of the P2P discovery engine (e.g. P2P discovery is temporarily suspended because the user disabled Bluetooth).
 *
 *  @param state    The current state of the P2P discovery engine. One of the values of <code> PPKPeer2PeerDiscoveryState. </code>
 *
 *  @see            <code> PPKPeer2PeerDiscoveryState </code>
 */
-(void)p2pDiscoveryStateChanged:(PPKPeer2PeerDiscoveryState)state;

/*!
 *  @abstract       Reports P2P discovery of a nearby peer.
 *
 *  @param peer     <code> PPKPeer </code> with unique ID and discovery info
 */
-(void)p2pPeerDiscovered:(PPKPeer*)peer;

/*!
 *  @abstract       Called if a recently discovered P2P-peer is no longer nearby. P2PKit tries to determine when a peer is no longer nearby on a best effort basis.
 *
 *  @param peer     <code> PPKPeer </code> with unique ID and discovery info
 */
-(void)p2pPeerLost:(PPKPeer*)peer;

/*!
 *  @abstract       Called if a discovered peer updated his discovery info.
 *
 *  @param peer     <code> PPKPeer </code> with unique ID and discovery info
 */
-(void)discoveryInfoUpdatedForPeer:(PPKPeer*)peer;

/*!
 *  @abstract       Called if the proximity strength for a peer changes.
 *
 *  @param peer     <code> PPKPeer </code> with unique ID and discovery info
 *
 *  @see            <code> PPKProximityStrength </code>
 */
-(void)proximityStrengthChangedForPeer:(PPKPeer*)peer;


/*!
 *  @name   GEO Discovery
 */
#pragma mark GEO Discovery

/*!
 *  @abstract       <b>(Beta API)</b><br/> Indicates a state change of the GEO discovery engine (e.g. GEO discovery is temporarily suspended due to lost internet connectivity).
 *
 *  @param state    The current state of the GEO discovery engine. One of the values of <code> PPKGeoDiscoveryState. </code>
 *
 *  @see            <code> PPKGeoDiscoveryState </code>
 */
-(void)geoDiscoveryStateChanged:(PPKGeoDiscoveryState)state;

/*!
 *  @abstract       <b>(Beta API)</b><br/> Reports GEO discovery of a nearby peer (i.e. your reported GEO location is 'nearby' the reported GEO location of the peer).
 *
 *  @param peerID   Unique ID of the peer
 */
-(void)geoPeerDiscovered:(NSString*)peerID;

/*!
 *  @abstract       <b>(Beta API)</b><br/> Called if a recently discovered GEO-peer is no longer nearby. P2PKit tries to determine when a peer is no longer nearby on a best effort basis.
 *
 *  @param peerID   Unique ID of the peer
 */
-(void)geoPeerLost:(NSString*)peerID;


/*!
 *  @name   Online Messaging
 */
#pragma mark Online Messaging

/*!
 *  @abstract       <b>(Beta API)</b><br/> Indicates a state change of the Online messaging engine (e.g. Online messaging is temporarily suspended due to lost internet connectivity).
 *
 *  @param state    The current state of the Online messaging engine. One of the values of <code> PPKOnlineMessagingState. </code>
 *
 *  @see            <code> PPKOnlineMessagingState </code>
 */
-(void)onlineMessagingStateChanged:(PPKOnlineMessagingState)state;

/*!
 *  @abstract               <b>(Beta API)</b><br/> Called when an online message is received from a remote peer.
 *
 *  @param  messageBody     Message body
 *  @param  messageHeader   Type of the message body (e.g. @"text-message") - apps can freely choose header values
 *  @param  peerID          Unique ID of the remote peer
 */
-(void)messageReceived:(NSData*)messageBody header:(NSString*)messageHeader from:(NSString*)peerID;

@end



#pragma mark - PPKController

@class CLLocation;

/*!
 *  <code> PPKController </code> is your entry point to P2PKit. You will interact with P2PKit via static methods, never try to obtain an instance of <code> PPKController. </code>
 */
@interface PPKController : NSObject


/*!
 *  @name   Lifecycle
 */
#pragma mark Lifecycle

/*!
 *  @abstract           Initializes the P2PKit. This method returns immediately.
 *
 *  @discussion         If the enabling is successful, <code> PPKControllerInitialized </code> is called.<br/>On a failure, <code> PPKControllerFailedWithError </code> is called.<br/>If p2pkit is already enabled, a consecutive call to this method will result in an exception.<br/><br/><b>Note:</b> Calling this method constitutes a P2PKit usage event.
 *
 *  @warning            This method must be called once before any other interaction with <code> PPKController </code>.<br/>If p2pkit is already initialized or the appKey is invalid, this method will throw an <code> NSException </code>.
 *
 *  @param appKey       The App Key you created. You can manage your App Keys in the <code><a href="https://p2pkit-console.uepaa.ch/">p2pkit console</a></code>
 *  @param observer     Your (partial) implementation of the <code> PPKControllerDelegate </code> protocol
 *
 *  @see                <code> PPKControllerDelegate </code>
 */
+(void)enableWithConfiguration:(NSString*)appKey observer:(id<PPKControllerDelegate>)observer;

/*!
 *  @abstract           Shuts-down the P2PKit.
 */
+(void)disable;

/*!
 *  @abstract           Use this to check if P2PKit is already enabled
 */
+(BOOL)isEnabled;

/*!
 *  @abstract           Returns the unique peer ID of the current device.
 *
 *  @return             The unique peer ID of the current device. P2PKit generates this id when you enable <code> PPKController </code> for the first time.
 */
+(NSString*)myPeerID;

/*!
 *  @abstract           Registers an additional observer.
 *
 *  @param observer     Your (partial) implementation of the <code> PPKControllerDelegate </code> protocol
 *
 *  @see                <code> PPKControllerDelegate </code>
 */
+(void)addObserver:(id<PPKControllerDelegate>)observer;

/*!
 *  @abstract           Removes an already registered observer.
 *
 *  @param observer     Registered observer
 */
+(void)removeObserver:(id<PPKControllerDelegate>)observer;


/*!
 *  @name   P2P Discovery
 */
#pragma mark P2P Discovery

/*!
 *  @abstract       Starts P2P discovery with discovery info (after successful startup, you will discover nearby P2P peers and will be discovered by nearby P2P peers).
 *
 *  @param info     <code> NSData </code> object, can be nil but not longer than 440 bytes. See <code> PPKPeer.discoveryInfo </code>
 *  @param enabled  Whether to enable CoreBluetooth State Preservation and Restoration (not supported on OS X)
 *
 *  @discussion     p2pkit can use the CoreBluetooth State Preservation and Restoration API. State restoration enables p2pkit-enabled apps to continue to discover and be discovered even if the application has crashed or was terminated by the OS. In order for state restoration to work, you would need to <code> startP2PDiscoveryWithDiscoveryInfo:stateRestoration: </code> when the application is relaunched.<br/>State Restoration is not supported on OS X.<br/><br/><strong>Important:</strong> Please make sure you <code> stopP2PDiscovery </code> when your end-user no longer wishes to discover or be discovered.
 *
 *  @warning        Please note that discovery info is exchanged publicly over P2P (BLE) and is unencrypted, do not send sensitive information over this API!<br/>If the discovery info is too long, this method will throw an <code>NSException</code>.
 */
+(void)startP2PDiscoveryWithDiscoveryInfo:(NSData*)info stateRestoration:(BOOL)enabled;

/*!
 *  @abstract       Updates your discovery info. The new discovery info is exchanged with other peers on a best effort basis and is not guaranteed.
 *
 *  @warning        Please note that discovery info is exchanged publicly over P2P (BLE) and is unencrypted, do not send sensitive information over this API!<br/>If the discovery info is too long, this method will throw an <code> NSException </code>.
 *
 *  @param info     <code> NSData </code> object, can be nil but not longer than 440 bytes. See <code> PPKPeer.discoveryInfo </code>
 */
+(void)pushNewP2PDiscoveryInfo:(NSData*)info;

/*!
 *  @abstract       Stops P2P discovery (you will no longer discover P2P peers and will no longer be discovered by P2P peers).
 */
+(void)stopP2PDiscovery;

/*!
 *  @abstract       Returns the current state of the P2P discovery engine.
 *
 *  @return         The current state of the P2P discovery engine.
 *
 *  @see            PPKPeer2PeerDiscoveryState
 */
+(PPKPeer2PeerDiscoveryState)p2pDiscoveryState;

/*!
 *  @abstract       Enables Proximity Ranging. Nearby P2P peers will be continuously ranged and associated with one of the levels of <code> PPKProximityStrength. </code>
 *
 *  @discussion     Updates will be delivered through the <code> proximityStrengthChangedForPeer: </code> delegate method.<br/><br/><strong>Note:</strong> Proximity ranging only works in the foreground. Not all Android devices have the capability to be ranged by other peers.<br/><br/><strong>Note:</strong> Proximity strength relies on the signal strength and can be affected by various factors in the environment.
 *
 *  @see            <code> PPKProximityStrength </code>
 */
+(void)enableProximityRanging;


/*!
 *  @name   GEO Discovery (Beta API)
 */
#pragma mark GEO Discovery (Beta API)

/*!
 *  @abstract               <b>(Beta API)</b><br/> Starts GEO discovery (after successful startup, you will discover nearby GEO peers and will be discovered by nearby GEO peers). You should periodically report your current GEO location for GEO discovery to work. Use: <code> updateUserLocation:. </code>
 *
 *  @see                    <code> updateUserLocation: </code>
 */
+(void)startGeoDiscovery;

/*!
 *  @abstract               <b>(Beta API)</b><br/> Informs the GEO discovery server about your most recent position. Should be called periodically.
 *
 *  @param location         <code> CLLocation </code> object containing the users location
 */
+(void)updateUserLocation:(CLLocation*)location;

/*!
 *  @abstract               <b>(Beta API)</b><br/> Stops GEO discovery (you will no longer discover GEO peers and will no longer be discovered by GEO peers).
 */
+(void)stopGeoDiscovery;

/*!
 *  @abstract               Returns the current state of the GEO discovery engine.
 *
 *  @return                 The current state of the GEO discovery engine.
 *
 *  @see                    <code> PPKGeoDiscoveryState </code>
 */
+(PPKGeoDiscoveryState)geoDiscoveryState;


/*!
 *  @name   Online Messaging (Beta API)
 */
#pragma mark Online Messaging (Beta API)

/*!
 *  @abstract               <b>(Beta API)</b><br/> Starts Online messaging (after successful startup, you can send and receive Online messages).
 */
+(void)startOnlineMessaging;

/*!
 *  @abstract               <b>(Beta API)</b><br/> Stops Online messaging (you will no longer be able to send or receive online messages).
 */
+(void)stopOnlineMessaging;

/*!
 *  @abstract               <b>(Beta API)</b><br/> Sends an online message via our cloud to a remote peer.
 *
 *  @discussion             Please note that message sending is fire and forget, if the recipient is not connected, the message is lost.
 *
 *  @param  messageBody     message body (max. 2 MB)
 *  @param  messageHeader   type of the message body (e.g. @"text-message") - apps can freely choose header values
 *  @param  peerID          unique id of the remote peer
 *
 *  @warning                Please note that data is unencrypted, do not send sensitive information over this API!.
 */
+(void)sendMessage:(NSData*)messageBody withHeader:(NSString*)messageHeader to:(NSString*)peerID;

/*!
 *  @abstract               Returns the current state of the Online messaging engine
 *
 *  @return                 The current state of the Online messaging engine
 *
 *  @see                    PPKOnlineMessagingState
 */
+(PPKOnlineMessagingState)onlineMessagingState;


/*!
 *  @name   Deprecated methods
 */
#pragma mark Deprecated methods

/*!
 *  @abstract       Starts P2P discovery with discovery info (after successful startup, you will discover nearby P2P peers and will be discovered by nearby P2P peers).
 *
 *  @param info     <code> NSData </code> object, can be nil but not longer than 440 bytes. See <code> PPKPeer.discoveryInfo </code>
 *
 *  @deprecated     This method is deprecated starting in version 1.2, please use <code> startP2PDiscoveryWithDiscoveryInfo:stateRestoration: </code> instead.
 */
+(void)startP2PDiscoveryWithDiscoveryInfo:(NSData*)info __deprecated_msg(" Please use startP2PDiscoveryWithDiscoveryInfo:stateRestoration: instead ");

@end
