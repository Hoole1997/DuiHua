package com.duihua.chat.util.test

import android.widget.ArrayAdapter
import android.widget.Spinner
import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView

/**
 * Spinner 的扩展函数
 * @param items 字符串列表
 * @param onItemSelected 选择项改变时的回调，参数为选中项的索引和值
 */
fun Spinner.setupWithStringList(
    items: List<String>,
    onItemSelected: ((position: Int, item: String) -> Unit)? = null
) {
    // 创建适配器
    val adapter = ArrayAdapter(
        context,
        R.layout.simple_spinner_item,
        items
    ).apply {
        // 设置下拉列表的样式
        setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    }

    // 设置适配器
    this.adapter = adapter

    // 设置选择监听
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            onItemSelected?.invoke(position, items[position])
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // 可以根据需要处理未选择的情况
        }
    }
}