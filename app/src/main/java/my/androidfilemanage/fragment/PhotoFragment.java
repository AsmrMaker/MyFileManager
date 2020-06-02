package my.androidfilemanage.fragment;

import android.app.ProgressDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import my.androidfilemanage.R;
import my.androidfilemanage.adapter.ExpandableItemAdapter;
import my.androidfilemanage.base.BaseFragment;
import my.androidfilemanage.bean.EventCenter;
import my.androidfilemanage.bean.FolderInfo;
import my.androidfilemanage.bean.SubItem;
import my.androidfilemanage.utils.LocalMediaLoader;

/**
 * Created by CWJ on 2017/3/21.
 */

public class PhotoFragment extends BaseFragment {
    private RecyclerView rlv_photo;
    private ExpandableItemAdapter mPhotoExpandableItemAdapter;
    private ArrayList<MultiItemEntity> mEntityArrayList = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    public boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public void onEventComming(EventCenter var1) {

    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_photo;
    }

    @Override
    public void initView() {
        rlv_photo = (RecyclerView) getActivity().findViewById(R.id.rlv_photo);
        rlv_photo.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPhotoExpandableItemAdapter = new ExpandableItemAdapter(mEntityArrayList, true);
        rlv_photo.setAdapter(mPhotoExpandableItemAdapter);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("数据加载中");
        progressDialog.setCancelable(false);
        progressDialog.show();  //将进度条显示出来
        new LocalMediaLoader(getActivity(), LocalMediaLoader.TYPE_IMAGE).loadAllImage(new LocalMediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<FolderInfo> folders) {
                progressDialog.dismiss();
                for (int i = 0; i < folders.size(); i++) {
                    SubItem subItem = new SubItem(folders.get(i).getName());
                    for (int j = 0; j < folders.get(i).getImages().size(); j++) {
                        subItem.addSubItem(folders.get(i).getImages().get(j));
                    }
                    mEntityArrayList.add(subItem);
                }
                mPhotoExpandableItemAdapter.notifyDataSetChanged();
            }
        });
    }
}
