/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.gephi.twitter.utils.listener;

/**
 *
 * @author totetmatt
 */
public interface ITweetsQueue<T> {

    T poll();

    void add(T streamingTweet);

    int getSize();
}
