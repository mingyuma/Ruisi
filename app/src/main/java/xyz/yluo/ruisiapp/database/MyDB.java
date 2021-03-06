package xyz.yluo.ruisiapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.yluo.ruisiapp.model.ArticleListData;
import xyz.yluo.ruisiapp.model.ForumListData;
import xyz.yluo.ruisiapp.model.SchoolNewsData;
import xyz.yluo.ruisiapp.utils.GetId;


public class MyDB {
    private Context context;
    public static final int MODE_READ = 0;
    public static final int MODE_WRITE = 1;
    /**
     * 浏览历史表
     */
    static final String TABLE_READ_HISTORY = "rs_article_list";
    /**
     * 板块列表 表
     */
    static final String TABLE_FORUM_LIST = "rs_forum_list";

    /**
     * 校内新闻数据表
     */
    static final String TABLE_SCHOOL_NEWS = "rs_news";

    /**
     * 消息列表
     */
    static final String TABLE_MESSAGE = "rs_message";

    private SQLiteDatabase db = null;    //数据库操作


    //构造函数
    public MyDB(Context context, int mode) {
        this.context = context;
        if (mode == MODE_WRITE) {
            this.db = new SQLiteHelper(context).getWritableDatabase();
        } else {
            this.db = new SQLiteHelper(context).getReadableDatabase();
        }
    }

    public void clearAllDataBase() {
        String sql = "DELETE FROM " + TABLE_READ_HISTORY;
        this.db.execSQL(sql);
        Log.e("mydb", "clear TABLE_READ_HISTORY");

        String sql2 = "DELETE FROM " + TABLE_READ_HISTORY;
        this.db.execSQL(sql2);

        String sql3 = "DELETE FROM " + TABLE_SCHOOL_NEWS;
        this.db.execSQL(sql3);

        String sql4 = "DELETE FROM " + TABLE_MESSAGE;
        this.db.execSQL(sql4);
        Log.e("mydb", "clear all TABLE_MESSAGE");
    }

    private String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());

        return format.format(curDate);
    }

    private Date getDate(String str) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        return format.parse(str);
    }

    //处理单个点击事件 判断是更新还是插入
    public void handSingleReadHistory(String tid, String title, String author) {
        if (null == title) {
            title = "null";
        }
        if (null == author) {
            author = "null";
        }
        if (isArticleRead(tid)) {
            updateReadHistory(tid, title, author);
        } else {
            insertReadHistory(tid, title, author);
        }
    }

    //判断list<> 是否为已读并修改返回
    public List<ArticleListData> handReadHistoryList(List<ArticleListData> datas) {
        String sql = "SELECT tid from " + TABLE_READ_HISTORY + " where tid = ?";
        for (ArticleListData data : datas) {
            String tid = GetId.getid("tid=",data.getTitleUrl());
            String args[] = new String[]{String.valueOf(tid)};
            Cursor result = db.rawQuery(sql, args);
            int count = result.getCount();
            result.close();
            if (count != 0)//判断得到的返回数据是否为空
            {
                data.setRead(true);
                Log.e("mydb", tid + "is read");
            }
        }
        return datas;
    }

    //判断插入数据的ID是否已经存在数据库中。
    private boolean isArticleRead(String tid) {
        String sql = "SELECT tid from " + TABLE_READ_HISTORY + " where tid = ?";
        String args[] = new String[]{String.valueOf(tid)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        result.close();
        return count != 0;
    }

    //	//插入操作
    private void insertReadHistory(String tid, String title, String author) {
        String sql = "INSERT INTO " + TABLE_READ_HISTORY + " (tid,title,author,read_time)"
                + " VALUES(?,?,?,?)";
        String read_time_str = getTime();
        Object args[] = new Object[]{tid, title, author, read_time_str};
        this.db.execSQL(sql, args);
        Log.e("mydb", tid + "insertReadHistory");
    }

    //更新操作
    private void updateReadHistory(String tid, String title, String author) {
        String read_time_str = getTime();
        String sql = "UPDATE " + TABLE_READ_HISTORY + " SET title=?,read_time=? WHERE tid=?";
        Object args[] = new Object[]{title, read_time_str, tid};
        this.db.execSQL(sql, args);
        Log.e("mydb", tid + "updateReadHistory" + author);
    }

    public void showHistoryDatabase() {
        String sql = "SELECT * FROM " + TABLE_READ_HISTORY;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext())    //采用循环的方式查询数据
        {
            Log.i("show database", result.getString(0) + "," + result.getString(1) + "," + result.getString(2) + "," + result.getString(3));
        }
        result.close();
    }

    public void deleteOldHistory(int num) {
        //最长缓存2000条数据 num 2000
        Cursor cursor = this.db.rawQuery("SELECT COUNT(*) FROM " + TABLE_READ_HISTORY, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        int a = count - num;
        if (a > 0) {
            //大于就一次性删除1/5
            a = num/5;
            //DELETE FROM XXX WHERE tid IN (SELECT TOP 100 PurchaseOrderDetailID FROM Purchasing.PurchaseOrderDetail
            //ORDER BY DueDate DESC);
            String sql = "DELETE FROM " + TABLE_READ_HISTORY + " WHERE tid IN (SELECT tid FROM " + TABLE_READ_HISTORY
                    + "  ORDER BY read_time ASC limit " + a + ")";
            this.db.rawQuery(sql, null);

            Log.e("阅读历史", "删除了最后" + a + "条记录");
        }

        this.db.close();
    }

    public List<ArticleListData> getHistory(int num) {
        List<ArticleListData> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_READ_HISTORY + " order by read_time desc limit " + num;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext())    //采用循环的方式查询数据
        {
            /**
             * tid VARCHAR(10) primary key,"
             + "title VARCHAR(50),"
             + "author VARCHAR(15),"
             + "read_time DATETIME,"
             */

            //boolean haveImage,String title, String titleUrl, String author, String replayCount
            datas.add(new ArticleListData(false, result.getString(1), result.getString(0), result.getString(2), "null", 0xff888888));
        }
        result.close();
        return datas;
    }


    /**
     * 设置板块列表到数据库
     */
    public void setForums(List<ForumListData> datas) {
        if (datas != null && datas.size() > 0) {
            clearForums();
            this.db = new SQLiteHelper(context).getWritableDatabase();
            for (ForumListData d : datas) {
                String sql = "INSERT INTO " + TABLE_FORUM_LIST + " (name,fid,todayNew,isHeader)"
                        + " VALUES(?,?,?,?)";
                int isHeader = d.isheader() ? 1 : 0;
                Object args[] = new Object[]{d.getTitle(), d.getFid(), d.getTodayNew(), isHeader};
                try {
                    this.db.execSQL(sql, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 获得板块列表
     */
    public List<ForumListData> getForums() {
        List<ForumListData> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_FORUM_LIST;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext())    //采用循环的方式查询数据
        {

            boolean isHeader = false;
            int isheader = result.getInt(3);
            if (isheader == 1) {
                isHeader = true;
            }
            int fid = result.getInt(1);
            String name = result.getString(0);
            String todayNew = result.getString(2);
            //String todayNew, String titleUrl
            datas.add(new ForumListData(isHeader, name, todayNew, fid));
        }
        result.close();
        return datas;
    }

    /**
     * 清空板块数据库
     */
    public void clearForums() {
        String sql = "DELETE FROM " + TABLE_FORUM_LIST;
        this.db.execSQL(sql);
    }


    /**
     * 新闻缓存
     */
    public void setSingleNewsRead(String url) {
        String sql = "UPDATE " + TABLE_SCHOOL_NEWS + " SET is_read =? WHERE url=?";
        Object args[] = new Object[]{1, url};
        this.db.execSQL(sql, args);
    }

    private boolean isNewsInDataBase(String url) {
        String sql = "SELECT * from " + TABLE_SCHOOL_NEWS + " where url = ?";
        String args[] = new String[]{String.valueOf(url)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        if (count == 0) {
            result.close();
            return false;
        } else {
            result.close();
            return true;
        }
    }

    private boolean isNewsRead(String url) {
        String sql = "SELECT * from " + TABLE_SCHOOL_NEWS + " where url = ?";
        String args[] = new String[]{String.valueOf(url)};
        Cursor result = db.rawQuery(sql, args);
        int count = result.getCount();
        result.moveToFirst();
        if (count <= 0) {//db.close();
            result.close();
            return false;
        } else {
            int i = result.getInt(5);
            result.close();
            return i == 1;
        }
    }

    /**
     * 传入为空 则返回所有的内容
     * 传入不为空 和传入的参数做比较，标记已读
     */
    public List<SchoolNewsData> getNewsList(List<SchoolNewsData> datasIn) {
        List<SchoolNewsData> datas = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_SCHOOL_NEWS;
        Cursor result = this.db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext())    //采用循环的方式查询数据
        {
            String url = result.getString(0);
            String title = result.getString(1);
            boolean isImage = (result.getInt(2) == 1);
            boolean is_patch = (result.getInt(3) == 1);
            String post_time = result.getString(4);
            boolean is_read = (result.getInt(5) == 1);
            //String url, String title, boolean is_image, boolean is_patch, String post_time
            datas.add(new SchoolNewsData(url, title, isImage, is_patch, post_time, is_read));
        }

        result.close();
        if (datasIn == null || datasIn.size() == 0) {
            return datas;
        } else {
            //传入参数不为空
            //插入新的数据
            for (SchoolNewsData d : datasIn) {
                //不在数据库
                if (!isNewsInDataBase(d.getUrl())) {
                    insertNews(d);
                } else {
                    //在数据库 设置是否已读
                    if (isNewsRead(d.getUrl())) {
                        d.setRead(true);
                    }
                }
            }
            return datasIn;
        }
    }

    private void insertNews(SchoolNewsData d) {
        String sql = "INSERT INTO " + TABLE_SCHOOL_NEWS + " (title,url,is_image,is_patch,post_time,is_read)"
                + " VALUES(?,?,?,?,?,?)";
        int is_read = d.isRead() ? 1 : 0;
        int is_image = d.is_image() ? 1 : 0;
        int is_patch = d.is_patch() ? 1 : 0;
        Object args[] = new Object[]{d.getTitle(), d.getUrl(), is_image, is_patch, d.getPost_time(), is_read};
        this.db.execSQL(sql, args);
    }

}