package uk.ac.wlv.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import uk.ac.wlv.criminalintent.database.CrimeBaseHelper;
import uk.ac.wlv.criminalintent.database.CrimeCursorWrapper;
import uk.ac.wlv.criminalintent.database.CrimeDbSchema;


public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private List<Crime> mCrimes;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mCrimes = new ArrayList<>();
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
//        for (int i = 0; i < 100; i++) {
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + i);
//            crime.setSolved(i % 2 == 0); // Every other one
//            mCrimes.add(crime);
//        }
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally{
                cursor.close();
            }
            return mCrimes;
        }

    public void addCrime(Crime c){
        ContentValues values = getContentValues (c);
        mDatabase.insert(CrimeDbSchema.CrimeTable.NAME, null, values);
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes (
                CrimeDbSchema.CrimeTable.Cols.UUID + "= ?",
                new String [] {id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst ();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void updateCrime (Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues (crime);
        mDatabase.update(CrimeDbSchema.CrimeTable.NAME, values,
                CrimeDbSchema.CrimeTable.Cols.UUID + "=?",
                new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrimes (String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeDbSchema.CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new CrimeCursorWrapper(cursor);
    }
    private static ContentValues getContentValues (Crime crime){
        ContentValues values = new ContentValues();
        values.put (CrimeDbSchema.CrimeTable.Cols.UUID, crime.getId().toString());
        values.put (CrimeDbSchema.CrimeTable.Cols.TITLE, crime.getTitle());
        values.put (CrimeDbSchema.CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put (CrimeDbSchema.CrimeTable.Cols.SOLVED, crime.isSolved() ? 1:0);
        return values;
    }
}

