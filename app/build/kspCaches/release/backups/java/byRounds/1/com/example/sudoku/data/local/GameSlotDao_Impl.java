package com.example.sudoku.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GameSlotDao_Impl implements GameSlotDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GameSlotEntity> __insertionAdapterOfGameSlotEntity;

  public GameSlotDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGameSlotEntity = new EntityInsertionAdapter<GameSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `game_slots` (`slotId`,`level`,`floor`,`chosenDifficulty`,`boardStateJson`,`undoStackJson`,`redoStackJson`,`elapsedSeconds`,`difficulty`,`activeThemeName`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GameSlotEntity entity) {
        statement.bindString(1, entity.getSlotId());
        statement.bindLong(2, entity.getLevel());
        statement.bindLong(3, entity.getFloor());
        statement.bindLong(4, entity.getChosenDifficulty());
        statement.bindString(5, entity.getBoardStateJson());
        statement.bindString(6, entity.getUndoStackJson());
        statement.bindString(7, entity.getRedoStackJson());
        statement.bindLong(8, entity.getElapsedSeconds());
        statement.bindDouble(9, entity.getDifficulty());
        statement.bindString(10, entity.getActiveThemeName());
      }
    };
  }

  @Override
  public Object insertSlot(final GameSlotEntity slot,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGameSlotEntity.insert(slot);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSlot(final String slotId,
      final Continuation<? super GameSlotEntity> $completion) {
    final String _sql = "SELECT * FROM game_slots WHERE slotId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, slotId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GameSlotEntity>() {
      @Override
      @Nullable
      public GameSlotEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "slotId");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfFloor = CursorUtil.getColumnIndexOrThrow(_cursor, "floor");
          final int _cursorIndexOfChosenDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "chosenDifficulty");
          final int _cursorIndexOfBoardStateJson = CursorUtil.getColumnIndexOrThrow(_cursor, "boardStateJson");
          final int _cursorIndexOfUndoStackJson = CursorUtil.getColumnIndexOrThrow(_cursor, "undoStackJson");
          final int _cursorIndexOfRedoStackJson = CursorUtil.getColumnIndexOrThrow(_cursor, "redoStackJson");
          final int _cursorIndexOfElapsedSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "elapsedSeconds");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfActiveThemeName = CursorUtil.getColumnIndexOrThrow(_cursor, "activeThemeName");
          final GameSlotEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSlotId;
            _tmpSlotId = _cursor.getString(_cursorIndexOfSlotId);
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpFloor;
            _tmpFloor = _cursor.getInt(_cursorIndexOfFloor);
            final int _tmpChosenDifficulty;
            _tmpChosenDifficulty = _cursor.getInt(_cursorIndexOfChosenDifficulty);
            final String _tmpBoardStateJson;
            _tmpBoardStateJson = _cursor.getString(_cursorIndexOfBoardStateJson);
            final String _tmpUndoStackJson;
            _tmpUndoStackJson = _cursor.getString(_cursorIndexOfUndoStackJson);
            final String _tmpRedoStackJson;
            _tmpRedoStackJson = _cursor.getString(_cursorIndexOfRedoStackJson);
            final long _tmpElapsedSeconds;
            _tmpElapsedSeconds = _cursor.getLong(_cursorIndexOfElapsedSeconds);
            final double _tmpDifficulty;
            _tmpDifficulty = _cursor.getDouble(_cursorIndexOfDifficulty);
            final String _tmpActiveThemeName;
            _tmpActiveThemeName = _cursor.getString(_cursorIndexOfActiveThemeName);
            _result = new GameSlotEntity(_tmpSlotId,_tmpLevel,_tmpFloor,_tmpChosenDifficulty,_tmpBoardStateJson,_tmpUndoStackJson,_tmpRedoStackJson,_tmpElapsedSeconds,_tmpDifficulty,_tmpActiveThemeName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
