package com.microsoft.cognitiveservices.speech.plugin;

import android.Manifest;
import android.util.Log;

import org.json.JSONArray;

import java.util.concurrent.Future;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;
import android.os.AsyncTask;

public class CognitiveServices extends CordovaPlugin {

    private SpeechConfig speechConfig;
    private SpeechSynthesizer synthesizer;

    private static final String LOGTAG = "CognitiveServices";

    private static final String subscriptionError = "Please run SetSubscription with the Cognitive Services subscription key and region.";

    private static final String INTERNET = Manifest.permission.INTERNET;
    private static final int INTERNET_REQ_CODE = 5;

    private final boolean speakStop = false;
    private MediaPlayer mediaPlayer;

    final int SAMPLE_RATE = 16000;

    final int BUFFER_SIZE = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

    private AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM);

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
        Log.d(LOGTAG, "Plugin Called: " + action);

        switch(action) {
            case "StartSpeaking":
                getPermission(INTERNET, INTERNET_REQ_CODE);
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        callbackContext.sendPluginResult(startSpeaking(data));
                    }
                });
                break;
            case "SpeakTextAsync":
                getPermission(INTERNET, INTERNET_REQ_CODE);
                callbackContext.sendPluginResult(SpeakTextAsync(data));
                break;
            case "SpeakSsml":
                getPermission(INTERNET, INTERNET_REQ_CODE);
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            callbackContext.sendPluginResult(SpeakSsml(data));
                        }
                    });
                break;
            case "SpeakSsmlAsync":
                getPermission(INTERNET, INTERNET_REQ_CODE);
                callbackContext.sendPluginResult(SpeakSsmlAsync(data));
                break;
            case "SetSubscription":
                callbackContext.sendPluginResult(setSubscription(data));
                break;
            default:
                callbackContext.sendPluginResult(new PluginResult(Status.ERROR, "Unexpected error calling Cognitive Services plugin"));
                }

        return true;
    }

    private PluginResult setSubscription(final JSONArray data) {

        // Initialize speech synthesizer and its dependencies
        try {
            final String speechSubscriptionKey = data.getString(0);
            final String serviceRegion = data.getString(1);
            speechConfig = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
        }
        catch (final Exception ex) {
            return new PluginResult(Status.ERROR, "Error setting cognitive services subscription. Error detail: " + System.lineSeparator()
                    + ex.getMessage());
        }

        try {
            synthesizer = new SpeechSynthesizer(speechConfig, null);
        }
        catch (final Exception err) {
            return new PluginResult(Status.ERROR, "Error setting creating speech synthesizer. Error detail: " + System.lineSeparator()
                    + err.getMessage());
        }

        return new PluginResult(Status.OK);
    }

    private void playAudio(AudioDataStream audioDataStream, Runnable callback) {
        

        LinkedList<byte[]> buffer = new LinkedList<>();
    
        byte[] data = new byte[BUFFER_SIZE];
        while (audioDataStream.readData(data) > 0) {
            synchronized (buffer) {
                buffer.add(data);
            }
            data = new byte[BUFFER_SIZE];
        }
        
        

        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
            byte[] sound = new byte[BUFFER_SIZE];
            while (buffer.size() > 0) {
                if (buffer.size() > 0) {
                    audioTrack.write(buffer.peek(), 0, sound.length);
                    synchronized (buffer) {
                        buffer.pop();
                    }
                }
            }
            audioTrack.stop();
            audioTrack.release();
        }

        if (callback != null) {
            callback.run();
        }
    
    }
    
    private PluginResult startSpeaking(final JSONArray data) {
        if (synthesizer == null) {
            return new PluginResult(Status.ERROR, subscriptionError);
        }

        try {
            SpeechSynthesisResult result = synthesizer.SpeakText(data.getString(0));
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                Log.d(LOGTAG, "Speech synthesis succeeded.");            
                playAudio(AudioDataStream.fromResult(result), null);
            }
            else if (result.getReason() == ResultReason.Canceled) {
                String cancellationDetails =
                        SpeechSynthesisCancellationDetails.fromResult(result).toString();
                        Log.d(LOGTAG, "Error synthesizing. Error detail: " +
                        cancellationDetails);
            }
            result.close();
            return setResult(result);
        } catch (final Exception ex) {
            return new PluginResult(Status.ERROR, "Speak Text error " + ex.getMessage());
        }
    }
    






    private PluginResult SpeakTextAsync(final JSONArray data) {
        if (synthesizer == null) {
            return new PluginResult(Status.ERROR, subscriptionError);
        }

        try {
            
         
            
            return new PluginResult(Status.OK);
                         //return setResult(result.get());
            
            
        } catch (final Exception ex) {
            return new PluginResult(Status.ERROR, "Speak Text error " + ex.getMessage());
        }
    }

    
    

    private PluginResult SpeakStop() {
        audioTrack.stop();
        return new PluginResult(Status.OK);

    }
    private PluginResult SpeakSsml(final JSONArray data) {
        if (synthesizer == null) {
            return new PluginResult(Status.ERROR, subscriptionError);
        }

        try {

            final SpeechSynthesisResult result = synthesizer.SpeakSsml(data.getString(0));
            return setResult(result);
        } catch (final Exception ex) {
            return new PluginResult(Status.ERROR, "Speak SSML error " + ex.getMessage());
        }
    }

    private PluginResult SpeakSsmlAsync(final JSONArray data) {
        if (synthesizer == null) {
            return new PluginResult(Status.ERROR, subscriptionError);
        }

        try {
            final Future<SpeechSynthesisResult> result = synthesizer.SpeakSsmlAsync(data.getString(0));
            return setResult(result.get());
        } catch (final Exception ex) {
            return new PluginResult(Status.ERROR, "Speak Text error " + ex.getMessage());
        }
    }

    private PluginResult setResult(final SpeechSynthesisResult result) {
        if (result.getReason() == ResultReason.Canceled) {
            final String cancellationDetails = SpeechSynthesisCancellationDetails.fromResult(result).toString();
            return new PluginResult(Status.ERROR, "Error synthesizing. Error detail: " + System.lineSeparator()
                    + cancellationDetails);
        }

        result.close();

        return new PluginResult(Status.OK);
    }

    protected void getPermission(final String strCode, final int requestCode) {
        if (!cordova.hasPermission(strCode)) {
            cordova.requestPermission(this, requestCode, strCode);
        }
    }


    @Override
    public void onDestroy() {
      // Release speech synthesizer and its dependencies
      synthesizer.close();
      speechConfig.close();
    }
}
