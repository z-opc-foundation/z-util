package com.zifang.util.core.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 自研 Cron 表达式解析器。
 * 支持 5/6/7 段 cron 语法：分 时 日 月 周 [年]。
 * 字段支持 *, , , - , / , ? , L , # 。
 */
public class CronExpression {

    private static final int YEAR = Calendar.YEAR;
    private static final int MONTH = Calendar.MONTH;
    private static final int DAY_OF_MONTH = Calendar.DAY_OF_MONTH;
    private static final int HOUR_OF_DAY = Calendar.HOUR_OF_DAY;
    private static final int MINUTE = Calendar.MINUTE;
    private static final int SECOND = Calendar.SECOND;
    private static final int DAY_OF_WEEK = Calendar.DAY_OF_WEEK;

    private final String expression;
    private final TimeZone timeZone;
    private final FieldSpec secondSpec;
    private final FieldSpec minuteSpec;
    private final FieldSpec hourSpec;
    private final FieldSpec dayOfMonthSpec;
    private final FieldSpec monthSpec;
    private final FieldSpec dayOfWeekSpec;
    private final FieldSpec yearSpec;
    private final boolean hasYear;

    public CronExpression(String expression) {
        this(expression, TimeZone.getDefault());
    }

    public CronExpression(String expression, TimeZone timeZone) {
        if (expression == null) {
            throw new IllegalArgumentException("cron expression must not be null");
        }
        this.expression = expression.trim();
        this.timeZone = timeZone == null ? TimeZone.getDefault() : timeZone;

        String[] parts = this.expression.split("\\s+");
        if (parts.length < 5 || parts.length > 7) {
            throw new IllegalArgumentException("Invalid cron (expected 5-7 fields): " + this.expression);
        }
        this.secondSpec = new FieldSpec(parts[0], 0, 59);
        this.minuteSpec = new FieldSpec(parts[1], 0, 59);
        this.hourSpec = new FieldSpec(parts[2], 0, 23);
        this.dayOfMonthSpec = new FieldSpec(parts[3], 1, 31);
        this.monthSpec = new FieldSpec(parts[4], 1, 12);
        this.dayOfWeekSpec = new FieldSpec(parts[5], 1, 7);
        if (parts.length >= 7) {
            this.yearSpec = new FieldSpec(parts[6], 1970, 2099);
            this.hasYear = true;
        } else {
            this.yearSpec = null;
            this.hasYear = false;
        }
    }

    public Date getNextValidTimeAfter(Date after) {
        if (after == null) after = new Date();
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(after);
        cal.add(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 366 * 8; i++) {
            if (!matchesYear(cal)) { advanceYear(cal); continue; }
            if (!matchesMonth(cal)) { advanceMonth(cal); continue; }
            if (!matchesDayOfMonth(cal) || !matchesDayOfWeek(cal)) { advanceDay(cal); continue; }
            if (!matchesHour(cal)) { advanceHour(cal); continue; }
            if (!matchesMinute(cal)) { advanceMinute(cal); continue; }
            if (!matchesSecond(cal)) { cal.add(Calendar.SECOND, 1); continue; }
            return cal.getTime();
        }
        return null;
    }

    public String getExpression() { return expression; }
    public TimeZone getTimeZone() { return timeZone; }

    private boolean matchesYear(Calendar c) { return !hasYear || yearSpec.matches(c.get(YEAR)); }
    private boolean matchesMonth(Calendar c) { return monthSpec.matches(c.get(MONTH) + 1); }
    private boolean matchesDayOfMonth(Calendar c) { return dayOfMonthSpec.matches(c.get(DAY_OF_MONTH)); }
    private boolean matchesDayOfWeek(Calendar c) { return dayOfWeekSpec.matches(c.get(DAY_OF_WEEK)); }
    private boolean matchesHour(Calendar c) { return hourSpec.matches(c.get(HOUR_OF_DAY)); }
    private boolean matchesMinute(Calendar c) { return minuteSpec.matches(c.get(MINUTE)); }
    private boolean matchesSecond(Calendar c) { return secondSpec.matches(c.get(SECOND)); }

    private void advanceYear(Calendar c) {
        c.set(YEAR, c.get(YEAR) + 1); c.set(MONTH, 0); c.set(DAY_OF_MONTH, 1);
        c.set(HOUR_OF_DAY, 0); c.set(MINUTE, 0); c.set(SECOND, 0);
    }
    private void advanceMonth(Calendar c) {
        c.set(MONTH, c.get(MONTH) + 1); c.set(DAY_OF_MONTH, 1);
        c.set(HOUR_OF_DAY, 0); c.set(MINUTE, 0); c.set(SECOND, 0);
    }
    private void advanceDay(Calendar c) {
        c.add(DAY_OF_MONTH, 1); c.set(HOUR_OF_DAY, 0); c.set(MINUTE, 0); c.set(SECOND, 0);
    }
    private void advanceHour(Calendar c) {
        c.set(MINUTE, 0); c.set(SECOND, 0);
        for (int h = c.get(HOUR_OF_DAY) + 1; h < 24; h++) {
            if (hourSpec.matches(h)) { c.set(HOUR_OF_DAY, h); return; }
        }
        advanceDay(c);
    }
    private void advanceMinute(Calendar c) {
        c.set(SECOND, 0);
        for (int m = c.get(MINUTE) + 1; m < 60; m++) {
            if (minuteSpec.matches(m)) { c.set(MINUTE, m); return; }
        }
        advanceHour(c);
    }

    @Override
    public String toString() { return "CronExpression[" + expression + "]"; }

    static final class FieldSpec {
        final int min, max;
        final boolean any, noSpecific, last;
        int[] values;
        FieldSpec(String token, int min, int max) {
            this.min = min; this.max = max;
            this.values = new int[0];
            if ("*".equals(token)) { this.any = true; this.noSpecific = false; this.last = false; return; }
            if ("?".equals(token)) { this.any = false; this.noSpecific = true; this.last = false; return; }
            if ("L".equalsIgnoreCase(token)) { this.any = false; this.noSpecific = false; this.last = true; return; }
            this.any = false; this.noSpecific = false; this.last = false;
            java.util.List<Integer> vs = new java.util.ArrayList<>();
            if (token.contains("/")) {
                String[] parts = token.split("/", 2);
                int step = Integer.parseInt(parts[1]);
                int start = "*".equals(parts[0]) ? min : parseFirst(parts[0]);
                for (int v = start; v <= max; v += step) vs.add(v);
            } else if (token.contains(",")) {
                for (String s : token.split(",")) addRange(vs, s);
            } else if (token.contains("-")) {
                addRange(vs, token);
            } else {
                vs.add(Integer.parseInt(token));
            }
            int[] arr = new int[vs.size()];
            for (int i = 0; i < vs.size(); i++) arr[i] = vs.get(i);
            this.values = arr;
        }
        private void addRange(java.util.List<Integer> vs, String s) {
            if (s.contains("/")) {
                String[] ps = s.split("/", 2);
                int a = Integer.parseInt(ps[0]);
                int st = Integer.parseInt(ps[1]);
                for (int v = a; v <= max; v += st) vs.add(v);
                return;
            }
            if (s.contains("-")) {
                String[] r = s.split("-", 2);
                int a = Integer.parseInt(r[0]); int b = Integer.parseInt(r[1]);
                for (int v = a; v <= b; v++) vs.add(v);
                return;
            }
            vs.add(Integer.parseInt(s));
        }
        private int parseFirst(String s) {
            if (s.contains("-")) return Integer.parseInt(s.split("-")[0]);
            if (s.contains(",")) return Integer.parseInt(s.split(",")[0]);
            return Integer.parseInt(s);
        }
        boolean matches(int v) {
            if (any) return true;
            if (noSpecific) return false;
            if (last) return v == max;
            for (int x : values) if (x == v) return true;
            return false;
        }
    }
}
