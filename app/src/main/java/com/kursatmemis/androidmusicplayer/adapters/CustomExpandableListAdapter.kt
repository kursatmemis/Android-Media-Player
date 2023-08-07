package com.kursatmemis.androidmusicplayer.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kursatmemis.androidmusicplayer.R
import com.kursatmemis.androidmusicplayer.models.MusicCategory


class CustomExpandableListAdapter(
    private val context: AppCompatActivity,
    private val musicCategories: List<MusicCategory>
) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return musicCategories.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        if (musicCategories[groupPosition].items == null) {
            return 0
        } else {
            return musicCategories[groupPosition].items!!.size
        }

    }

    override fun getGroup(groupPosition: Int): Any {
        return musicCategories[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return musicCategories[groupPosition].items!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val layoutInflater = context.layoutInflater
        val groupView = layoutInflater.inflate(R.layout.listview_group_item, null)
        val groupItemTextTextView = groupView.findViewById<TextView>(R.id.groupItemTextTextView)
        groupItemTextTextView.text = musicCategories[groupPosition].baseTitle
        return groupView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val layoutInflater = context.layoutInflater
        val childView = layoutInflater.inflate(R.layout.listview_child_item, null)
        val childItemTextTextView = childView.findViewById<TextView>(R.id.childItemTextTextView)
        childItemTextTextView.text = musicCategories[groupPosition].items?.get(childPosition)?.title
        return childView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}