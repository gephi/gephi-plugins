/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.gephi.twitter.utils.listener;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author totetmatt
 */
public class LinkedListTweetsQueue<T> implements ITweetsQueue<T> {

    private final LinkedBlockingQueue<T> tweetsQueue = new LinkedBlockingQueue<>();
    private static final Logger logger = Logger.getLogger(LinkedListTweetsQueue.class.toString());

    @Override
    public T poll() {
        try {
            return tweetsQueue.take();
        } catch (InterruptedException ex) {
            logger.log(Level.WARNING, ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public void add(T streamingTweet) {
        tweetsQueue.add(streamingTweet);
    }

    @Override
    public int getSize() {
        return this.tweetsQueue.size();
    }
}
