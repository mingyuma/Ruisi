package xyz.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import xyz.yluo.ruisiapp.R;
import xyz.yluo.ruisiapp.View.ArrowTextView;
import xyz.yluo.ruisiapp.View.CircleImageView;
import xyz.yluo.ruisiapp.activity.ChatActivity;
import xyz.yluo.ruisiapp.activity.PostActivity;
import xyz.yluo.ruisiapp.activity.UserDetailActivity;
import xyz.yluo.ruisiapp.model.ListType;
import xyz.yluo.ruisiapp.model.MessageData;

/**
 * Created by free2 on 16-3-21.
 * 首页第三页
 * 回复我的 我的消息
 */
public class MessageAdapter extends BaseAdapter {
    protected Activity activity;
    private List<MessageData> DataSet;

    public MessageAdapter(Activity activity, List<MessageData> dataSet) {
        DataSet = dataSet;
        this.activity = activity;
    }

    @Override
    protected int getDataCount() {
        return DataSet.size();
    }

    @Override
    protected int getItemType(int pos) {
        return 0;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new MessageReplyListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messgae, parent, false));

    }


    //用户消息、回复我的 holder
    private class MessageReplyListHolder extends BaseViewHolder {
        protected TextView title;
        protected TextView time;
        CircleImageView article_user_image;
        ArrowTextView reply_content;
        TextView isRead;

        //url
        MessageReplyListHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            time = (TextView) itemView.findViewById(R.id.time);
            article_user_image = (CircleImageView) itemView.findViewById(R.id.article_user_image);
            reply_content = (ArrowTextView) itemView.findViewById(R.id.reply_content);
            isRead = (TextView) itemView.findViewById(R.id.is_read);

            article_user_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user_click();
                }
            });
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item_click();
                }
            });
        }

        void setData(int position) {
            MessageData single_data = DataSet.get(position);
            title.setText(single_data.getTitle());
            time.setText(single_data.getTime());
            String imageUrl = single_data.getauthorImage();
            Picasso.with(activity).load(imageUrl).placeholder(R.drawable.image_placeholder).into(article_user_image);
            reply_content.setText(single_data.getcontent());
            if (single_data.isRead()) {
                isRead.setVisibility(View.GONE);
            } else {
                isRead.setVisibility(View.VISIBLE);
            }
        }

        void item_click() {
            MessageData single_data = DataSet.get(getAdapterPosition());
            if (!single_data.isRead()) {
                single_data.setRead(true);
                notifyItemChanged(getAdapterPosition());
            }
            if (ListType.MYMESSAGE == single_data.getType()) {//用户消息pm
                String username = single_data.getTitle().replace("我对 ", "").replace("说:", "").replace(" 对我", "");
                ChatActivity.open(activity, username, single_data.getTitleUrl());
                single_data.setRead(true);
            } else if (ListType.REPLAYME == single_data.getType()) {//回复我的
                PostActivity.open(activity, single_data.getTitleUrl(), null);
            }

        }

        void user_click() {
            MessageData single_data = DataSet.get(getAdapterPosition());
            String username = single_data.getTitle().replace("我对 ", "").replace("说:", "").replace(" 对我", "").replace(" 回复了我", "");
            UserDetailActivity.openWithAnimation(
                    activity, username, article_user_image, DataSet.get(getAdapterPosition()).getauthorImage());
        }
    }

}