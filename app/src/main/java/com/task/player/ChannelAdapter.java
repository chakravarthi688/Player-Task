package com.task.player;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ItemRowHolder> {

    private JSONArray channelsArray;
    private MainActivity mainActivity;
    private Context context;
    private int mSelectedItem = 0, prevSelectedItem = 0;
    private RecyclerView tvChannelView;
    private Animation zoomInAnimation, zoomOutAnimation;

    ChannelAdapter(MainActivity tvActivity, JSONArray channelsArray, Context context) {
        this.mainActivity = tvActivity;
        this.context = context;
        this.channelsArray = channelsArray;
        zoomInAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_in_channel);
        zoomOutAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_out);
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.channel_item, viewGroup, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemRowHolder itemRowHolder, int position) {
        try {
            JSONObject channelObject = channelsArray.getJSONObject(itemRowHolder.getAdapterPosition());

            itemRowHolder.channelTitle.setText(channelObject.has("channelName") ? channelObject.getString("channelName") : "NA");
            itemRowHolder.channelNumber.setText(channelObject.has("channelDialNumber") ?
                    channelObject.getString("channelDialNumber") : "NA");
            Picasso.get().load(getChannelImage(channelObject)).into(itemRowHolder.channelIcon);

            if(itemRowHolder.getAdapterPosition() == mSelectedItem) {
                itemRowHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
                itemRowHolder.channelTitle.setTextColor(context.getResources().getColor(R.color.white));
                itemRowHolder.channelNumber.setTextColor(context.getResources().getColor(R.color.white));
                itemRowHolder.itemView.startAnimation(zoomInAnimation);
            } else {
                itemRowHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
                itemRowHolder.channelTitle.setTextColor(context.getResources().getColor(R.color.black));
                itemRowHolder.channelNumber.setTextColor(context.getResources().getColor(R.color.black));
                itemRowHolder.itemView.clearAnimation();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemRowHolder.itemView.setTranslationZ(0);
                }
            }

            itemRowHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemRowHolder.getAdapterPosition() != prevSelectedItem) {
                        try {
                            JSONObject channelObj = channelsArray.getJSONObject(mSelectedItem);
                            if(channelObj.has("streamingURL_HEVC")) {
                                prevSelectedItem = itemRowHolder.getAdapterPosition();
                                mainActivity.startPlayback(channelObj.getString("streamingURL_HEVC"));
                            } else {
                                Toast.makeText(context, context.getString(R.string.playback_error), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.current_channel_playback_message), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            itemRowHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus) {
                        v.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
                        itemRowHolder.channelTitle.setTextColor(context.getResources().getColor(R.color.white));
                        itemRowHolder.channelNumber.setTextColor(context.getResources().getColor(R.color.white));
                        v.startAnimation(zoomInAnimation);
                    } else {
                        v.setBackgroundColor(context.getResources().getColor(R.color.white));
                        itemRowHolder.channelTitle.setTextColor(context.getResources().getColor(R.color.black));
                        itemRowHolder.channelNumber.setTextColor(context.getResources().getColor(R.color.black));
                        v.clearAnimation();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            v.setTranslationZ(0);
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(position == 0) {
            itemRowHolder.itemView.requestFocus();
        }
    }

    private String getChannelImage(JSONObject channelObject) {
        String imageUrl = "";
        try {
            if(channelObject.has("images")) {
                JSONObject imageObject = channelObject.getJSONObject("images");
                if(imageObject.has("110*110")) {
                    imageUrl = imageObject.getString("110*110");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUrl;
    }

    @Override
    public int getItemCount() {
        return channelsArray.length();
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_channel_name) TextView channelTitle;
        @BindView(R.id.iv_channel_icon) ImageView channelIcon;
        @BindView(R.id.tv_channel_number) TextView channelNumber;

        ItemRowHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        tvChannelView = recyclerView;

        tvChannelView.setOnKeyListener((v, keyCode, event) -> {
            RecyclerView.LayoutManager lm = tvChannelView.getLayoutManager();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isConfirmButton(event)) {
                    if ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) == KeyEvent.FLAG_LONG_PRESS) {
                        tvChannelView.findViewHolderForAdapterPosition(mSelectedItem).itemView.performLongClick();
                    } else {
                        event.startTracking();
                    }
                    return true;
                } else {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if(lm != null) {
                            lm.findViewByPosition(mSelectedItem).setBackgroundColor(context.getResources().getColor(R.color.white));
                            lm.findViewByPosition(mSelectedItem).clearAnimation();
                            TextView chName = lm.findViewByPosition(mSelectedItem).findViewById(R.id.tv_channel_name);
                            TextView chNumber = lm.findViewByPosition(mSelectedItem).findViewById(R.id.tv_channel_number);
                            chName.setTextColor(context.getResources().getColor(R.color.black));
                            chNumber.setTextColor(context.getResources().getColor(R.color.black));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                lm.getChildAt(mSelectedItem).setTranslationZ(0);
                            }
                            lm.getChildAt(mSelectedItem).clearFocus();
                            mainActivity.setSelectedItem(1);
                            mainActivity.setChannelPosition(mSelectedItem);
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            } else if(event.getAction() == KeyEvent.ACTION_UP && isConfirmButton(event)
                    && ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != KeyEvent.FLAG_LONG_PRESS)) {
                tvChannelView.findViewHolderForAdapterPosition(mSelectedItem).itemView.performClick();
                return true;
            }
            return true;
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int nextSelectItem = mSelectedItem + direction;

        if (nextSelectItem >= 0 && nextSelectItem < getItemCount()) {
            notifyItemChanged(mSelectedItem);
            mSelectedItem = nextSelectItem;
            notifyItemChanged(mSelectedItem);
            lm.scrollToPosition(mSelectedItem);
            return true;
        }

        return false;
    }

    private static boolean isConfirmButton(KeyEvent event){
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                return true;
            default:
                return false;
        }
    }
}