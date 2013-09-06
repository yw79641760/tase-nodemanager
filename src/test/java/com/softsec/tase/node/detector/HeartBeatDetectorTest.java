/**
 * 
 */
package com.softsec.tase.node.detector;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * HeartBeatDetectorTest
 * <p> </p>
 * @author yanwei
 * @since 2013-8-21 下午7:43:46
 * @version
 */
public class HeartBeatDetectorTest extends TestCase {

	@Test
	public void testHeartbeart() {
		HeartBeatDetector detector = new HeartBeatDetector();
		System.out.println(detector.generateHeartBeat());
	}
}
