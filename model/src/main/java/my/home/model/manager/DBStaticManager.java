/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.home.model.manager;

import android.content.Context;
import android.util.Log;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;
import my.home.model.datasource.ChatItemDao;
import my.home.model.datasource.DaoMaster;
import my.home.model.datasource.DaoMaster.OpenHelper;
import my.home.model.datasource.DaoSession;
import my.home.model.datasource.HistoryItemDao;
import my.home.model.datasource.ShortcutDao;
import my.home.model.entities.ChatItem;
import my.home.model.entities.HistoryItem;
import my.home.model.entities.Shortcut;

public class DBStaticManager {
    private static final String TAG = DBStaticManager.class.getName();
    private static DaoMaster DAOMASTER;
    private static DaoSession DAOSESSION;
    private final static Object LOCK = new Object();

    public static void initManager(Context context) {
        if (DAOMASTER == null) {
            synchronized (LOCK) {
                if (DAOMASTER == null) {
                    OpenHelper helper = new DaoMaster.DevOpenHelper(context, "lehome_db", null);
                    DAOMASTER = new DaoMaster(helper.getWritableDatabase());
                    DAOSESSION = DAOMASTER.newSession();
                }
            }
        }
    }

    public static DaoMaster getDaoMaster(Context context) {
        initManager(context);
        if (DAOMASTER == null) {
            Log.w(TAG, "initManager must be call first.");
        }
        return DAOMASTER;
    }

    public static DaoSession getDaoSession(Context context) {
        initManager(context);
        if (DAOSESSION == null) {
            Log.w(TAG, "initManager must be call first.");
        }
        return DAOSESSION;
    }

    public static void addChatItem(Context context, ChatItem entity) {
        getDaoSession(context).getChatItemDao().insert(entity);
    }

    public static void updateChatItem(Context context, ChatItem entity) {
        getDaoSession(context).getChatItemDao().update(entity);
    }

    public static List<ChatItem> getAllChatItems(Context context) {
        return getDaoSession(context).getChatItemDao().loadAll();
    }

    public static List<ChatItem> loadLatest(Context context, int limit) {
        if (limit <= 0) {
            Log.w(TAG, "loadAfter invaild limit.");
            return null;
        }
        QueryBuilder<ChatItem> queryBuilder = getDaoSession(context).getChatItemDao().queryBuilder();
        return queryBuilder
                .orderDesc(ChatItemDao.Properties.Id)
                .limit(limit)
                .list();
    }

    public static List<ChatItem> loadBefore(Context context, long id, int limit) {
        if (limit <= 0) {
            Log.w(TAG, "loadAfter invaild limit.");
            return null;
        }
        QueryBuilder<ChatItem> queryBuilder = getDaoSession(context).getChatItemDao().queryBuilder();
        return queryBuilder
                .where(ChatItemDao.Properties.Id.lt(id))
                .orderDesc(ChatItemDao.Properties.Id)
                .limit(limit)
                .list();
    }

    public static void addShortcut(Context context, Shortcut shortcut) {
        if (shortcut.getId() == null || !hasShortcut(context, shortcut)) {
            getDaoSession(context).getShortcutDao().insert(shortcut);
        }
    }

    public static void updateShortcut(Context context, Shortcut shortcut) {
        getDaoSession(context).getShortcutDao().update(shortcut);
    }

    public static boolean hasShortcut(Context context, Shortcut shortcut) {
        QueryBuilder<ChatItem> queryBuilder = getDaoSession(context).getChatItemDao().queryBuilder();
        queryBuilder.where(ChatItemDao.Properties.Content.eq(shortcut.getContent()))
                .limit(1);
        return queryBuilder.buildCount().count() > 0 ? true : false;
    }

    public static List<Shortcut> getAllShortcuts(Context context) {
        return getDaoSession(context).getShortcutDao().loadAll();
    }

    public static void deleteShortcut(Context context, long Id) {
        QueryBuilder<Shortcut> qb = getDaoSession(context).getShortcutDao().queryBuilder();
        DeleteQuery<Shortcut> bd = qb.where(ShortcutDao.Properties.Id.eq(Id)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public static void addHistoryItem(Context context, HistoryItem item) {
        getDaoSession(context).getHistoryItemDao().insert(item);
    }

    public static List<HistoryItem> getLatestItems(Context context, String from, int limit) {
        if (limit <= 0) {
            Log.w(TAG, "getLatestItems invaild limit.");
            return null;
        }
        QueryBuilder<HistoryItem> queryBuilder = getDaoSession(context).getHistoryItemDao().queryBuilder();
        return queryBuilder
                .where(HistoryItemDao.Properties.From.eq(from))
                .orderAsc(HistoryItemDao.Properties.Id)
                .limit(limit)
                .list();
    }

    public static void destory() {
        if (DAOSESSION != null) {
            DAOSESSION.clear();
        }
        DAOMASTER = null;
        DAOSESSION = null;
    }
}
