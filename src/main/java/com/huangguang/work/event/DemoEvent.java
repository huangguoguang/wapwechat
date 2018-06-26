package com.huangguang.work.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created with IntelliJ IDEA.
 * <p>
 * Description:
 * User : huangguang
 * DATE : 2018-06-22 14:44
 */
public class DemoEvent extends ApplicationEvent{
    public DemoEvent(Object source) {
        super(source);
    }
}
