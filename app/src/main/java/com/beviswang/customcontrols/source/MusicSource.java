package com.beviswang.customcontrols.source;

import android.content.Context;

import com.beviswang.customcontrols.source.model.MusicModel;

import java.util.Iterator;

/**
 * 音乐源接口
 * 实现接口方法，内容提供者将迭代器中的音乐model提供给播放器和列表
 * Created by shize on 2017/3/28.
 */

public interface MusicSource {
    Iterator<MusicModel> iterators();
    Context getContext();
}
