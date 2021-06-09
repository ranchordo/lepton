package org.lepton.engine.audio;

import static org.lwjgl.openal.ALC11.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.openal.EXTThreadLocalContext.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Objects;

import org.lepton.util.Util;
import org.lepton.util.advancedLogger.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbisInfo;

import org.lwjgl.stb.STBVorbis;


/**
 * Handles master audio functionality as well as getting ogg files as playable sounds.
 */
public class Audio {
	public static float masterVolume=1;
	private static long device;
	private static long context;
	/**
	 * Find an audio device, initialize, and prepare it for playing.
	 */
	public static void init() {
		device=alcOpenDevice((ByteBuffer)null);
		if(device==NULL) {
			throw new IllegalStateException("Can't open audio device");
		}
		ALCCapabilities devcaps=ALC.createCapabilities(device);
		if(!devcaps.OpenALC10) {
			throw new IllegalStateException();
		}
		Logger.log(0,"Performing driver ALC support check:");
		Logger.log(0,"OpenALC10: "+devcaps.OpenALC10);
		Logger.log(0,"OpenALC11: "+devcaps.OpenALC11);
		Logger.log(0,"ALC_EXT_EFX: "+devcaps.ALC_EXT_EFX);
		
		if(devcaps.OpenALC11) {
			List<String> devs=ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);
			if(devs==null) {
				int err = alcGetError(NULL);
		        if (err != ALC_NO_ERROR) {
		            throw new RuntimeException(alcGetString(NULL, err));
		        }
			} else {
				Logger.log(0,"Performing device fetch:");
				for(int i=0;i<devs.size();i++) {
					Logger.log(0,i+": "+devs.get(i));
				}
			}
		}
		String defaultDevSpecifier=Objects.requireNonNull(alcGetString(NULL,ALC_DEFAULT_DEVICE_SPECIFIER));
		Logger.log(0,defaultDevSpecifier+": Performing ALCINFO fetch:");
		
		context=alcCreateContext(device,(IntBuffer)null);
		alcSetThreadContext(context);
		AL.createCapabilities(devcaps);
		
		Logger.log(0,"ALC_FREQUENCY: " + alcGetInteger(device, ALC_FREQUENCY) + "Hz");
        Logger.log(0,"ALC_REFRESH: " + alcGetInteger(device, ALC_REFRESH) + "Hz");
        Logger.log(0,"ALC_SYNC: " + (alcGetInteger(device, ALC_SYNC) == ALC_TRUE));
        Logger.log(0,"ALC_MONO_SOURCES: " + alcGetInteger(device, ALC_MONO_SOURCES));
        Logger.log(0,"ALC_STEREO_SOURCES: " + alcGetInteger(device, ALC_STEREO_SOURCES));
        
        Logger.log(0,"Initializing listener");
	}
	public static void cleanUp() {
		alcMakeContextCurrent(NULL);
		AL.setCurrentThread(null);
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
	//Mostly from the LWJGL 3.2.3 OPENAL demo:
	/**
	 * Gets an audio file with a specific filename. Automatically adds the extension. Uses {@link org.hpjmci.util.Util.getOptionallyIntegratedStream} internally.
	 * @param filename
	 * @return OpenGL style int pointer representing the audio buffer
	 */
	public static int getOGG(String file) {
		int buffer=alGenBuffers();
		try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readVorbis(file, 32 * 1024, info);

            //copy to buffer
            alBufferData(buffer, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
		return buffer;
	}
	private static ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) {
        ByteBuffer vorbis;
        try {
            vorbis = ioResourceToByteBuffer(resource, bufferSize);
        } catch (IOException e) {
            Logger.log(4,"IOException when opening "+resource+". Maybe the file doesn't exist?");
            return null;
        }

        IntBuffer error   = BufferUtils.createIntBuffer(1);
        long      decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
        if (decoder == NULL) {
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
        }

        STBVorbis.stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        ShortBuffer pcm = BufferUtils.createShortBuffer(STBVorbis.stb_vorbis_stream_length_in_samples(decoder) * channels);

        STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
        STBVorbis.stb_vorbis_close(decoder);
        return pcm;
    }
	private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;
		InputStream source=Util.getOptionallyIntegratedStream(resource,".ogg");
		ReadableByteChannel rbc = Channels.newChannel(source);
		buffer = BufferUtils.createByteBuffer(bufferSize);

        while (true) {
            int bytes = rbc.read(buffer);
            if (bytes == -1) {
                break;
            }
            if (buffer.remaining() == 0) {
                buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
            }
        }

        buffer.flip();
        return buffer;
    }
	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
	//End
}
