package kr.co.company.hw3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class BaseAdpaterMusic extends BaseAdapter {
    Context  mContext =null;
    List<Music> mData=null;
    LayoutInflater mLayoutInflater = null;

    public BaseAdpaterMusic(Context mContext, List<Music> mData) {
        this.mContext = mContext;
        this.mData = mData;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Music getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemLayout = convertView;
        ViewHolder viewHolder = null;
        if (itemLayout == null) {
            itemLayout = mLayoutInflater.inflate(R.layout.list_view_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) itemLayout.findViewById(R.id.title);
            viewHolder.musicImg= (ImageView) itemLayout.findViewById(R.id.musicImg);
            itemLayout.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) itemLayout.getTag();

        viewHolder.musicImg.setImageBitmap(mData.get(position).getAlbumArt());
        viewHolder.title.setText(mData.get(position).getTitle());

        return itemLayout;
    }

    class ViewHolder{
        TextView title;
        ImageView musicImg;
    }
}
