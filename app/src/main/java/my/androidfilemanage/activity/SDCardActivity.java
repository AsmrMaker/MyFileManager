package my.androidfilemanage.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import my.androidfilemanage.R;
import my.androidfilemanage.adapter.MultipleItem;
import my.androidfilemanage.adapter.MultipleItemQuickAdapter;
import my.androidfilemanage.base.baseActivity;
import my.androidfilemanage.bean.EventCenter;
import my.androidfilemanage.bean.FileDao;
import my.androidfilemanage.bean.FileInfo;
import my.androidfilemanage.utils.FileUtil;
import my.androidfilemanage.view.CheckBox;
import my.androidfilemanage.view.DividerItemDecoration;

import static my.androidfilemanage.utils.FileUtil.fileFilter;
import static my.androidfilemanage.utils.FileUtil.getFileInfosFromFileArray;

public class SDCardActivity extends baseActivity {
    @Bind(R.id.rlv_sd_card)
    RecyclerView rlv_sd_card;
    @Bind(R.id.tv_path)
    TextView tv_path;
    @Bind(R.id.tv_all_size)
    TextView tv_all_size;
    @Bind(R.id.tv_send)
    TextView tv_send;
    private List<FileInfo> fileInfos = new ArrayList<>();
    private List<MultipleItem> mMultipleItems = new ArrayList<>();
    private MultipleItemQuickAdapter mAdapter;
    private File mCurrentPathFile = null;
    private File mSDCardPath = null;
    private String path;

    @OnClick(R.id.iv_title_back)
    void iv_title_back() {
        if (mSDCardPath.getAbsolutePath().equals(mCurrentPathFile.getAbsolutePath())) {
            finish();
        } else {
            mCurrentPathFile = mCurrentPathFile.getParentFile();
            showFiles(mCurrentPathFile);
        }
    }

    @Bind(R.id.tv_title_middle)
    TextView tv_title_middle;

    @Override
    public void onEventComming(EventCenter var1) {

    }

    @Override
    public boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void initViewAndEvent() {

        FileDao.deleteAll1();

        tv_all_size.setText(getString(R.string.size, "0B"));
        tv_send.setText(getString(R.string.send, "0"));

        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("------------------------a-a-a---aa");
                System.out.println("------------------------a-a-a---aa"+FileDao.queryAll());
                System.out.println("------------------------a-a-a---aa"+FileDao.queryAll().size());

                List<FileInfo> files = FileDao.queryAll();
                if(files!=null&&files.size()>0)
                {
                    try
                    {
                        ArrayList<Uri> list = new ArrayList<Uri>();
                        for(FileInfo file:files)
                        {
                            File ff = new File(file.getFilePath());
                            Uri uri = FileProvider.getUriForFile(
                                    SDCardActivity.this,
                                    "my.androidfilemanage.fileprovider",
                                    ff);
                            list.add(uri);
                        }
                        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);//发送多个文件
                        intent.setType("*/*");//多个文件格式
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,list);//Intent.EXTRA_STREAM同于传输文件流
                        startActivity(intent);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });




        path = getIntent().getStringExtra("path");
        tv_title_middle.setText(getIntent().getStringExtra("name"));
        mSDCardPath = new File(path);
        rlv_sd_card.setLayoutManager(new LinearLayoutManager(this));
        rlv_sd_card.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, R.drawable.divide_line));
        mAdapter = new MultipleItemQuickAdapter(mMultipleItems);
        rlv_sd_card.setAdapter(mAdapter);
        showFiles(mSDCardPath);
        updateSizAndCount();

//        rlv_sd_card.setLongClickable(true);
//        rlv_sd_card.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return false;
//            }
//        });
        rlv_sd_card.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {

                if (adapter.getItemViewType(position) == MultipleItem.FILE)
                {
                    boolean isCheck = fileInfos.get(position).getIsCheck();
                    fileInfos.get(position).setIsCheck(!isCheck);
                    if (fileInfos.get(position).getIsCheck()) {
                        FileDao.insertFile(fileInfos.get(position));
                        ((CheckBox) view.findViewById(R.id.cb_file)).setChecked(true, true);
                    } else {
                        FileDao.deleteFile(fileInfos.get(position));
                        ((CheckBox) view.findViewById(R.id.cb_file)).setChecked(false, true);
                    }
                    EventBus.getDefault().post(new EventCenter<>(3));
                    updateSizAndCount();
                } else {
                    showFiles(new File(fileInfos.get(position).getFilePath()));
                }

            }


            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (adapter.getItemViewType(position) == MultipleItem.FILE)
                {
                    String path = fileInfos.get(position).getFilePath();
                    String name = fileInfos.get(position).getFileName();

                    Intent txtIntent = new Intent(getApplicationContext(), TextFileViewActivity.class);
                    txtIntent.putExtra("filePath", path);
                    txtIntent.putExtra("fileName", name);
                    startActivity(txtIntent);

                }

            }



        });
    }

    public void updateSizAndCount() {
        List<FileInfo> mList = FileDao.queryAll();
        if (mList.size() == 0) {
            tv_send.setBackgroundResource(R.drawable.shape_bt_send);
            tv_send.setTextColor(getResources().getColor(R.color.md_grey_700));
            tv_all_size.setText(getString(R.string.size, "0B"));
        } else {
            tv_send.setBackgroundResource(R.drawable.shape_bt_send_blue);
            tv_send.setTextColor(getResources().getColor(R.color.md_white_1000));
            long count = 0L;
            for (int i = 0; i < mList.size(); i++) {
                count = count + mList.get(i).getFileSize();
            }
            tv_all_size.setText(getString(R.string.size, FileUtil.FormetFileSize(count)));
        }
        tv_send.setText(getString(R.string.send, "" + mList.size()));
    }

    @Override
    public void onBackPressed() {
        if (mSDCardPath.getAbsolutePath().equals(mCurrentPathFile.getAbsolutePath())) {
            finish();
        } else {
            mCurrentPathFile = mCurrentPathFile.getParentFile();
            showFiles(mCurrentPathFile);
        }
    }

    private void showFiles(File folder) {
        FileDao.deleteAll1();
        updateSizAndCount();

        mMultipleItems.clear();
        tv_path.setText(folder.getAbsolutePath());
        mCurrentPathFile = folder;
        File[] files = fileFilter(folder);
        if (null == files || files.length == 0) {
            mAdapter.setEmptyView(getEmptyView());
            Log.e("files", "files::为空啦");
        } else {
            //获取文件信息
            fileInfos = getFileInfosFromFileArray(files);
            for (int i = 0; i < fileInfos.size(); i++) {
                if (fileInfos.get(i).isDirectory) {
                    mMultipleItems.add(new MultipleItem(MultipleItem.FOLD, fileInfos.get(i)));
                } else {
                    mMultipleItems.add(new MultipleItem(MultipleItem.FILE, fileInfos.get(i)));
                }

            }
            //查询本地数据库，如果之前有选择的就显示打钩
            List<FileInfo> mList = FileDao.queryAll();
            for (int i = 0; i < fileInfos.size(); i++) {
                for (FileInfo fileInfo : mList) {
                    if (fileInfo.getFileName().equals(fileInfos.get(i).getFileName())) {
                        fileInfos.get(i).setIsCheck(true);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private View getEmptyView() {
        return getLayoutInflater().inflate(R.layout.empty_view, (ViewGroup) rlv_sd_card.getParent(), false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_sdcard;
    }


}
