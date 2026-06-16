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
import java.lang.Integer;
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
public final class SeedPuzzleDao_Impl implements SeedPuzzleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SeedPuzzleEntity> __insertionAdapterOfSeedPuzzleEntity;

  public SeedPuzzleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSeedPuzzleEntity = new EntityInsertionAdapter<SeedPuzzleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `seed_puzzles` (`id`,`puzzleString`,`solutionString`,`difficulty`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SeedPuzzleEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPuzzleString());
        statement.bindString(3, entity.getSolutionString());
        statement.bindDouble(4, entity.getDifficulty());
      }
    };
  }

  @Override
  public Object insertSeeds(final List<SeedPuzzleEntity> seeds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSeedPuzzleEntity.insert(seeds);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRandomSeedInDifficultyRange(final double minDiff, final double maxDiff,
      final Continuation<? super SeedPuzzleEntity> $completion) {
    final String _sql = "SELECT * FROM seed_puzzles WHERE difficulty >= ? AND difficulty <= ? ORDER BY RANDOM() LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, minDiff);
    _argIndex = 2;
    _statement.bindDouble(_argIndex, maxDiff);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SeedPuzzleEntity>() {
      @Override
      @Nullable
      public SeedPuzzleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPuzzleString = CursorUtil.getColumnIndexOrThrow(_cursor, "puzzleString");
          final int _cursorIndexOfSolutionString = CursorUtil.getColumnIndexOrThrow(_cursor, "solutionString");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final SeedPuzzleEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPuzzleString;
            _tmpPuzzleString = _cursor.getString(_cursorIndexOfPuzzleString);
            final String _tmpSolutionString;
            _tmpSolutionString = _cursor.getString(_cursorIndexOfSolutionString);
            final double _tmpDifficulty;
            _tmpDifficulty = _cursor.getDouble(_cursorIndexOfDifficulty);
            _result = new SeedPuzzleEntity(_tmpId,_tmpPuzzleString,_tmpSolutionString,_tmpDifficulty);
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

  @Override
  public Object countSeeds(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM seed_puzzles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
