#import <Cordova/CDV.h>
#import <MicrosoftCognitiveServicesSpeech/SPXSpeechApi.h>
#import <AVFoundation/AVFoundation.h>

@interface CDVCognitiveServices : CDVPlugin {

AVAudioPlayer *player;
SPXSpeechConfiguration *speechConfig;
NSString *listenerCallbackId;
SPXSpeechRecognizer* speechRecognizer;

}

- (void)SetSubscription: (CDVInvokedUrlCommand*)command;
- (void)RecognizeFromMicrophone: (CDVInvokedUrlCommand*)command;
- (void)SpeakSsml: (CDVInvokedUrlCommand*)command;
- (void)StartSpeaking: (CDVInvokedUrlCommand*)command;
- (void)StopListening: (CDVInvokedUrlCommand*)command;
- (void)SpeakStop: (CDVInvokedUrlCommand*)command;

@end