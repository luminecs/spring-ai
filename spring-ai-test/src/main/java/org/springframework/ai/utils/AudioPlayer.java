package org.springframework.ai.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public final class AudioPlayer {

	private AudioPlayer() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static void main(String[] args) throws Exception {

		play(new BufferedInputStream(new FileInputStream(
				"/Users/christiantzolov/Dev/projects/spring-ai/models/spring-ai-openai/output.wav")));
	}

	public static void play(byte[] data) {
		play(new BufferedInputStream(new ByteArrayInputStream(data)));
	}

	public static void play(InputStream data) {

		try {
			try (AudioInputStream audio = AudioSystem.getAudioInputStream(data); Clip clip = AudioSystem.getClip()) {
				clip.open(audio);
				clip.start();

				while (!clip.isRunning()) {
					Thread.sleep(100);
				}

				while (clip.isRunning()) {
					Thread.sleep(3000);
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
