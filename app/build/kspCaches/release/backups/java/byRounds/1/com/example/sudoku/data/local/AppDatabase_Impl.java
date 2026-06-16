package com.example.sudoku.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile GameSlotDao _gameSlotDao;

  private volatile SeedPuzzleDao _seedPuzzleDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `game_slots` (`slotId` TEXT NOT NULL, `level` INTEGER NOT NULL, `floor` INTEGER NOT NULL, `chosenDifficulty` INTEGER NOT NULL, `boardStateJson` TEXT NOT NULL, `undoStackJson` TEXT NOT NULL, `redoStackJson` TEXT NOT NULL, `elapsedSeconds` INTEGER NOT NULL, `difficulty` REAL NOT NULL, `activeThemeName` TEXT NOT NULL, PRIMARY KEY(`slotId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `seed_puzzles` (`id` TEXT NOT NULL, `puzzleString` TEXT NOT NULL, `solutionString` TEXT NOT NULL, `difficulty` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6fbf8dc30a510726584ef683f3a8cb82')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `game_slots`");
        db.execSQL("DROP TABLE IF EXISTS `seed_puzzles`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsGameSlots = new HashMap<String, TableInfo.Column>(10);
        _columnsGameSlots.put("slotId", new TableInfo.Column("slotId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("level", new TableInfo.Column("level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("floor", new TableInfo.Column("floor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("chosenDifficulty", new TableInfo.Column("chosenDifficulty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("boardStateJson", new TableInfo.Column("boardStateJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("undoStackJson", new TableInfo.Column("undoStackJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("redoStackJson", new TableInfo.Column("redoStackJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("elapsedSeconds", new TableInfo.Column("elapsedSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("difficulty", new TableInfo.Column("difficulty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSlots.put("activeThemeName", new TableInfo.Column("activeThemeName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGameSlots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGameSlots = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGameSlots = new TableInfo("game_slots", _columnsGameSlots, _foreignKeysGameSlots, _indicesGameSlots);
        final TableInfo _existingGameSlots = TableInfo.read(db, "game_slots");
        if (!_infoGameSlots.equals(_existingGameSlots)) {
          return new RoomOpenHelper.ValidationResult(false, "game_slots(com.example.sudoku.data.local.GameSlotEntity).\n"
                  + " Expected:\n" + _infoGameSlots + "\n"
                  + " Found:\n" + _existingGameSlots);
        }
        final HashMap<String, TableInfo.Column> _columnsSeedPuzzles = new HashMap<String, TableInfo.Column>(4);
        _columnsSeedPuzzles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeedPuzzles.put("puzzleString", new TableInfo.Column("puzzleString", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeedPuzzles.put("solutionString", new TableInfo.Column("solutionString", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeedPuzzles.put("difficulty", new TableInfo.Column("difficulty", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSeedPuzzles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSeedPuzzles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSeedPuzzles = new TableInfo("seed_puzzles", _columnsSeedPuzzles, _foreignKeysSeedPuzzles, _indicesSeedPuzzles);
        final TableInfo _existingSeedPuzzles = TableInfo.read(db, "seed_puzzles");
        if (!_infoSeedPuzzles.equals(_existingSeedPuzzles)) {
          return new RoomOpenHelper.ValidationResult(false, "seed_puzzles(com.example.sudoku.data.local.SeedPuzzleEntity).\n"
                  + " Expected:\n" + _infoSeedPuzzles + "\n"
                  + " Found:\n" + _existingSeedPuzzles);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6fbf8dc30a510726584ef683f3a8cb82", "82fa6ee9c03611fc5a59acf72bca854e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "game_slots","seed_puzzles");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `game_slots`");
      _db.execSQL("DELETE FROM `seed_puzzles`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(GameSlotDao.class, GameSlotDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SeedPuzzleDao.class, SeedPuzzleDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public GameSlotDao gameSlotDao() {
    if (_gameSlotDao != null) {
      return _gameSlotDao;
    } else {
      synchronized(this) {
        if(_gameSlotDao == null) {
          _gameSlotDao = new GameSlotDao_Impl(this);
        }
        return _gameSlotDao;
      }
    }
  }

  @Override
  public SeedPuzzleDao seedPuzzleDao() {
    if (_seedPuzzleDao != null) {
      return _seedPuzzleDao;
    } else {
      synchronized(this) {
        if(_seedPuzzleDao == null) {
          _seedPuzzleDao = new SeedPuzzleDao_Impl(this);
        }
        return _seedPuzzleDao;
      }
    }
  }
}
