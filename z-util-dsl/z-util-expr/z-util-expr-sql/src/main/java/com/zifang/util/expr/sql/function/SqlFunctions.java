package com.zifang.util.expr.sql.function;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.annotation.SqlFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 内置 SQL 函数集。
 * 所有方法必须是 static，第一个参数为当前行 Map<String, Object>，其余为函数实参。
 */
public final class SqlFunctions {

    private SqlFunctions() {
    }

    // ===================== 数学函数 =====================

    @SqlFunction("ABS")
    public static Object abs(Map<String, Object> row, Object n) {
        if (n == null) return null;
        if (n instanceof Number) {
            double v = ((Number) n).doubleValue();
            return v < 0 ? -v : v;
        }
        return Math.abs(Double.parseDouble(n.toString()));
    }

    @SqlFunction("ROUND")
    public static Object round(Map<String, Object> row, Object n, Object scale) {
        if (n == null) return null;
        int s = scale == null ? 0 : Integer.parseInt(scale.toString());
        BigDecimal bd = new BigDecimal(n.toString()).setScale(s, RoundingMode.HALF_UP);
        return s == 0 ? bd.intValue() : bd.doubleValue();
    }

    @SqlFunction("FLOOR")
    public static Object floor(Map<String, Object> row, Object n) {
        if (n == null) return null;
        return Math.floor(Double.parseDouble(n.toString()));
    }

    @SqlFunction("CEIL")
    public static Object ceil(Map<String, Object> row, Object n) {
        if (n == null) return null;
        return Math.ceil(Double.parseDouble(n.toString()));
    }

    @SqlFunction("MOD")
    public static Object mod(Map<String, Object> row, Object a, Object b) {
        if (a == null || b == null) return null;
        double av = Double.parseDouble(a.toString());
        double bv = Double.parseDouble(b.toString());
        return av % bv;
    }

    @SqlFunction("POWER")
    public static Object power(Map<String, Object> row, Object base, Object exp) {
        if (base == null || exp == null) return null;
        return Math.pow(Double.parseDouble(base.toString()), Double.parseDouble(exp.toString()));
    }

    @SqlFunction("SQRT")
    public static Object sqrt(Map<String, Object> row, Object n) {
        if (n == null) return null;
        return Math.sqrt(Double.parseDouble(n.toString()));
    }

    @SqlFunction("LOG")
    public static Object log(Map<String, Object> row, Object n) {
        if (n == null) return null;
        return Math.log(Double.parseDouble(n.toString()));
    }

    @SqlFunction("LOG10")
    public static Object log10(Map<String, Object> row, Object n) {
        if (n == null) return null;
        return Math.log10(Double.parseDouble(n.toString()));
    }

    @SqlFunction("EXP")
    public static Object exp(Map<String, Object> row, Object n) {
        if (n == null) return null;
        return Math.exp(Double.parseDouble(n.toString()));
    }

    @SqlFunction("MAX")
    public static Object max(Map<String, Object> row, Object... args) {
        if (args == null || args.length == 0) return null;
        double max = Double.NaN;
        for (Object arg : args) {
            if (arg != null) {
                double v = arg instanceof Number ? ((Number) arg).doubleValue() : Double.parseDouble(arg.toString());
                if (Double.isNaN(max)) max = v;
                else if (v > max) max = v;
            }
        }
        return Double.isNaN(max) ? null : max;
    }

    @SqlFunction("MIN")
    public static Object min(Map<String, Object> row, Object... args) {
        if (args == null || args.length == 0) return null;
        double min = Double.NaN;
        for (Object arg : args) {
            if (arg != null) {
                double v = arg instanceof Number ? ((Number) arg).doubleValue() : Double.parseDouble(arg.toString());
                if (Double.isNaN(min)) min = v;
                else if (v < min) min = v;
            }
        }
        return Double.isNaN(min) ? null : min;
    }

    @SqlFunction("SIGN")
    public static Object sign(Map<String, Object> row, Object n) {
        if (n == null) return null;
        double v = ((Number) n).doubleValue();
        return v > 0 ? 1 : v < 0 ? -1 : 0;
    }

    // ===================== 字符串函数 =====================

    @SqlFunction("UPPER")
    public static Object upper(Map<String, Object> row, Object s) {
        return s == null ? null : s.toString().toUpperCase();
    }

    @SqlFunction("LOWER")
    public static Object lower(Map<String, Object> row, Object s) {
        return s == null ? null : s.toString().toLowerCase();
    }

    @SqlFunction("TRIM")
    public static Object trim(Map<String, Object> row, Object s) {
        return s == null ? null : s.toString().trim();
    }

    @SqlFunction("LTRIM")
    public static Object ltrim(Map<String, Object> row, Object s) {
        return s == null ? null : s.toString().replaceFirst("^\\s+", "");
    }

    @SqlFunction("RTRIM")
    public static Object rtrim(Map<String, Object> row, Object s) {
        return s == null ? null : s.toString().replaceFirst("\\s+$", "");
    }

    @SqlFunction("LENGTH")
    public static Object length(Map<String, Object> row, Object s) {
        return s == null ? null : s.toString().length();
    }

    @SqlFunction("SUBSTRING")
    public static Object substring(Map<String, Object> row, Object s, Object start, Object len) {
        if (s == null) return null;
        String str = s.toString();
        int from = Integer.parseInt(start.toString()) - 1; // SQL is 1-based
        int l = len == null ? str.length() - from : Integer.parseInt(len.toString());
        if (from < 0) from = 0;
        if (from >= str.length()) return "";
        l = Math.min(l, str.length() - from);
        return str.substring(from, from + l);
    }

    @SqlFunction("CONCAT")
    public static Object concat(Map<String, Object> row, Object... args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) sb.append(arg.toString());
        }
        return sb.toString();
    }

    @SqlFunction("CONCAT_WS")
    public static Object concat_ws(Map<String, Object> row, Object sep, Object... args) {
        if (sep == null) return concat(row, args);
        String separator = sep.toString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                if (sb.length() > 0) sb.append(separator);
                sb.append(args[i].toString());
            }
        }
        return sb.toString();
    }

    @SqlFunction("REPLACE")
    public static Object replace(Map<String, Object> row, Object s, Object from, Object to) {
        if (s == null) return null;
        return s.toString().replace(from.toString(), to.toString());
    }

    @SqlFunction("REVERSE")
    public static Object reverse(Map<String, Object> row, Object s) {
        if (s == null) return null;
        return new StringBuilder(s.toString()).reverse().toString();
    }

    @SqlFunction("LPAD")
    public static Object lpad(Map<String, Object> row, Object s, Object len, Object pad) {
        if (s == null) return null;
        String str = s.toString();
        int desired = Integer.parseInt(len.toString());
        String p = pad == null ? " " : pad.toString();
        while (str.length() < desired) {
            str = p + str;
        }
        return str;
    }

    @SqlFunction("RPAD")
    public static Object rpad(Map<String, Object> row, Object s, Object len, Object pad) {
        if (s == null) return null;
        String str = s.toString();
        int desired = Integer.parseInt(len.toString());
        String p = pad == null ? " " : pad.toString();
        while (str.length() < desired) {
            str = str + p;
        }
        return str;
    }

    @SqlFunction("INSTR")
    public static Object instr(Map<String, Object> row, Object haystack, Object needle) {
        if (haystack == null || needle == null) return null;
        return haystack.toString().indexOf(needle.toString()) + 1;
    }

    @SqlFunction("SUBSTR")
    public static Object substr(Map<String, Object> row, Object s, Object start, Object len) {
        return substring(row, s, start, len);
    }

    @SqlFunction("CHAR_LENGTH")
    public static Object char_length(Map<String, Object> row, Object s) {
        return length(row, s);
    }

    @SqlFunction("INITCAP")
    public static Object initcap(Map<String, Object> row, Object s) {
        if (s == null) return null;
        String str = s.toString();
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c) || c == '_' || c == '-') {
                nextUpper = true;
                sb.append(c);
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    @SqlFunction("LEFT")
    public static Object left(Map<String, Object> row, Object s, Object n) {
        if (s == null) return null;
        String str = s.toString();
        int count = Integer.parseInt(n.toString());
        return str.substring(0, Math.min(count, str.length()));
    }

    @SqlFunction("RIGHT")
    public static Object right(Map<String, Object> row, Object s, Object n) {
        if (s == null) return null;
        String str = s.toString();
        int count = Integer.parseInt(n.toString());
        return str.substring(Math.max(0, str.length() - count));
    }

    @SqlFunction("SPACE")
    public static Object space(Map<String, Object> row, Object n) {
        if (n == null) return null;
        int count = Integer.parseInt(n.toString());
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(' ');
        return sb.toString();
    }

    @SqlFunction("ASCII")
    public static Object ascii(Map<String, Object> row, Object s) {
        if (s == null || s.toString().isEmpty()) return null;
        return (int) s.toString().charAt(0);
    }

    @SqlFunction("CHAR")
    public static Object char_(Map<String, Object> row, Object code) {
        if (code == null) return null;
        return Character.toString((char) Integer.parseInt(code.toString()));
    }

    // ===================== 日期/时间函数 =====================

    @SqlFunction("NOW")
    public static Object now(Map<String, Object> row, Object... args) {
        java.time.ZoneId zone = resolveZone(safeArg(args, 0));
        java.time.LocalDateTime ldt = LocalDateTime.now(zone);
        Object fmtArg = safeArg(args, 1);
        if (fmtArg != null) {
            return ldt.format(DateTimeFormatter.ofPattern(fmtArg.toString()));
        }
        return ldt;
    }

    /**
     * 当前时刻的 epoch 毫秒数（Long）。配合 EL 的 {@code /} 自动提升小数，
     * 数值时间戳列可直接 {@code WHERE ts >= NOW_MS() - 7*86400000}。
     */
    @SqlFunction("NOW_MS")
    public static Object now_ms(Map<String, Object> row, Object... args) {
        java.time.ZoneId zone = resolveZone(safeArg(args, 0));
        return java.time.ZonedDateTime.now(zone).toInstant().toEpochMilli();
    }

    /**
     * {@code DATE_SUB(d, 7, 'DAY')} 与 {@link #date_add} 同义；语义上保留减号更直观。
     */
    @SqlFunction("DATE_SUB")
    public static Object date_sub(Map<String, Object> row, Object date, Object interval, Object unit) {
        if (interval == null) return date;
        long n = Long.parseLong(interval.toString());
        return date_add(row, date, -n, unit);
    }

    @SqlFunction("CURDATE")
    public static Object curdate(Map<String, Object> row, Object... args) {
        java.time.ZoneId zone = resolveZone(safeArg(args, 0));
        java.time.LocalDate ld = LocalDate.now(zone);
        Object fmtArg = safeArg(args, 1);
        if (fmtArg != null) {
            return ld.format(DateTimeFormatter.ofPattern(fmtArg.toString()));
        }
        return ld;
    }

    @SqlFunction("TODAY")
    public static Object today(Map<String, Object> row, Object... args) {
        return curdate(row, args);
    }

    @SqlFunction("CURTIME")
    public static Object curtime(Map<String, Object> row, Object... args) {
        java.time.ZoneId zone = resolveZone(safeArg(args, 0));
        java.time.LocalTime lt = LocalTime.now(zone);
        Object fmtArg = safeArg(args, 1);
        if (fmtArg != null) {
            return lt.format(DateTimeFormatter.ofPattern(fmtArg.toString()));
        }
        return lt;
    }

    private static Object safeArg(Object[] args, int index) {
        return args != null && args.length > index ? args[index] : null;
    }

    private static java.time.ZoneId resolveZone(Object arg) {
        if (arg == null) {
            return java.time.ZoneId.systemDefault();
        }
        String s = arg.toString().trim();
        if (s.isEmpty()) {
            return java.time.ZoneId.systemDefault();
        }
        try {
            return java.time.ZoneId.of(s);
        } catch (Exception e) {
            return java.time.ZoneId.systemDefault();
        }
    }

    @SqlFunction("YEAR")
    public static Object year(Map<String, Object> row, Object date) {
        return temporalField(date, java.time.temporal.ChronoField.YEAR);
    }

    @SqlFunction("MONTH")
    public static Object month(Map<String, Object> row, Object date) {
        return temporalField(date, java.time.temporal.ChronoField.MONTH_OF_YEAR);
    }

    @SqlFunction("DAY")
    public static Object day(Map<String, Object> row, Object date) {
        return temporalField(date, java.time.temporal.ChronoField.DAY_OF_MONTH);
    }

    @SqlFunction("HOUR")
    public static Object hour(Map<String, Object> row, Object date) {
        return temporalField(date, java.time.temporal.ChronoField.HOUR_OF_DAY);
    }

    @SqlFunction("MINUTE")
    public static Object minute(Map<String, Object> row, Object date) {
        return temporalField(date, java.time.temporal.ChronoField.MINUTE_OF_HOUR);
    }

    @SqlFunction("SECOND")
    public static Object second(Map<String, Object> row, Object date) {
        return temporalField(date, java.time.temporal.ChronoField.SECOND_OF_MINUTE);
    }

    /**
     * 按字段取日期成分。纯日期输入的时间字段按 MySQL 同义返回 0（HOUR('2026-08-21') = 0）。
     */
    private static Integer temporalField(Object date, java.time.temporal.ChronoField field) {
        if (date == null) return null;
        try {
            if (date instanceof LocalTime) {
                return field.isTimeBased() ? ((LocalTime) date).get(field) : null;
            }
            return toLocalDateTime(date).get(field);
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("DATE")
    public static Object date(Map<String, Object> row, Object s) {
        if (s == null) return null;
        String str = s.toString();
        try {
            if (str.contains("-")) {
                return LocalDate.parse(str.substring(0, 10));
            }
        } catch (Exception e) { /* fallthrough */ }
        return s;
    }

    /**
     * MySQL DATE_FORMAT 词法：{@code %Y-%m-%d} 等百分号词法翻译成 java pattern；
     * 不含 % 的 pattern 按原 java DateTimeFormatter 语义透传（向后兼容）。
     */
    @SqlFunction("DATE_FORMAT")
    public static Object date_format(Map<String, Object> row, Object date, Object fmt) {
        if (date == null || fmt == null) return null;
        String pattern = fmt.toString();
        try {
            java.time.temporal.TemporalAccessor t;
            if (date instanceof LocalDateTime || date instanceof LocalDate || date instanceof LocalTime) {
                t = (java.time.temporal.TemporalAccessor) date;
            } else if (date instanceof java.util.Date) {
                t = ((java.util.Date) date).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            } else {
                String s = date.toString().trim();
                t = toLocalDateTime(s);
            }
            DateTimeFormatter f = pattern.indexOf('%') >= 0
                    ? DateTimeFormatter.ofPattern(mysqlPatternToJava(pattern), Locale.ENGLISH)
                    : DateTimeFormatter.ofPattern(pattern);
            return f.format(t);
        } catch (Exception e) {
            return date.toString();
        }
    }

    /**
     * MySQL → java 词法映射表；未映射的字母按 java pattern 字面量引号包住，
     * 使 {@code '%Y年%m月'} 里的中文/字母不会被误当 java pattern 词法。
     */
    private static String mysqlPatternToJava(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length() + 16);
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c != '%') {
                appendLiteralSafe(sb, c);
                continue;
            }
            if (++i >= pattern.length()) {
                sb.append('%');
                break;
            }
            char m = pattern.charAt(i);
            switch (m) {
                case 'Y': sb.append("yyyy"); break;
                case 'y': sb.append("yy"); break;
                case 'm': sb.append("MM"); break;
                case 'c': sb.append('M'); break;
                case 'd': sb.append("dd"); break;
                case 'e': sb.append('d'); break;
                case 'H': sb.append("HH"); break;
                case 'k': sb.append('H'); break;
                case 'h': case 'I': case 'l': sb.append("hh"); break;
                case 'i': sb.append("mm"); break;
                case 's': case 'S': sb.append("ss"); break;
                case 'f': sb.append("SSSSSS"); break;
                case 'p': sb.append('a'); break;
                case 'M': sb.append("MMMM"); break;
                case 'b': sb.append("MMM"); break;
                case 'W': sb.append("EEEE"); break;
                case 'a': sb.append('E'); break;
                case 'j': sb.append("DDD"); break;
                case 'T': sb.append("HH:mm:ss"); break;
                case 'r': sb.append("hh:mm:ss a"); break;
                case '%': sb.append('\'').append('%').append('\''); break;
                default: sb.append('\'').append('%').append(m).append('\''); break;
            }
        }
        return sb.toString();
    }

    private static void appendLiteralSafe(StringBuilder sb, char c) {
        if (Character.isLetter(c) || c == '\'') {
            if (c == '\'') {
                sb.append("''");
            } else {
                sb.append('\'').append(c).append('\'');
            }
        } else {
            sb.append(c);
        }
    }

    @SqlFunction("DATEDIFF")
    public static Object datediff(Map<String, Object> row, Object a, Object b) {
        if (a == null || b == null) return null;
        try {
            LocalDate da = a instanceof LocalDate ? (LocalDate) a : LocalDate.parse(a.toString().substring(0, 10));
            LocalDate db = b instanceof LocalDate ? (LocalDate) b : LocalDate.parse(b.toString().substring(0, 10));
            return java.time.temporal.ChronoUnit.DAYS.between(db, da);
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("DAYOFWEEK")
    public static Object dayofweek(Map<String, Object> row, Object date) {
        if (date == null) return null;
        try {
            LocalDate ld = date instanceof LocalDate ? (LocalDate) date : LocalDate.parse(date.toString().substring(0, 10));
            return ld.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday (ISO)
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("WEEK")
    public static Object week(Map<String, Object> row, Object date) {
        if (date == null) return null;
        try {
            LocalDate ld = date instanceof LocalDate ? (LocalDate) date : LocalDate.parse(date.toString().substring(0, 10));
            return ld.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("MONTHNAME")
    public static Object monthname(Map<String, Object> row, Object date) {
        if (date == null) return null;
        try {
            LocalDate ld = date instanceof LocalDate ? (LocalDate) date : LocalDate.parse(date.toString().substring(0, 10));
            return ld.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("DAYNAME")
    public static Object dayname(Map<String, Object> row, Object date) {
        if (date == null) return null;
        try {
            LocalDate ld = date instanceof LocalDate ? (LocalDate) date : LocalDate.parse(date.toString().substring(0, 10));
            return ld.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("DATE_ADD")
    public static Object date_add(Map<String, Object> row, Object date, Object interval, Object unit) {
        if (date == null) return null;
        try {
            long amount = Long.parseLong(interval.toString());
            String u = unit.toString().toUpperCase();
            if (date instanceof LocalDate) {
                LocalDate ld = (LocalDate) date;
                switch (u) {
                    case "DAY":
                        return ld.plusDays(amount);
                    case "MONTH":
                        return ld.plusMonths(amount);
                    case "YEAR":
                        return ld.plusYears(amount);
                    case "HOUR":
                    case "MINUTE":
                    case "SECOND":
                        return ld.atStartOfDay().plus(amount, java.time.temporal.ChronoUnit.valueOf(u));
                    default:
                        return ld.plusDays(amount);
                }
            }
            LocalDateTime ldt = toLocalDateTime(date);
            switch (u) {
                case "DAY":
                    return ldt.plusDays(amount);
                case "MONTH":
                    return ldt.plusMonths(amount);
                case "YEAR":
                    return ldt.plusYears(amount);
                case "HOUR":
                    return ldt.plusHours(amount);
                case "MINUTE":
                    return ldt.plusMinutes(amount);
                case "SECOND":
                    return ldt.plusSeconds(amount);
                default:
                    return ldt.plusDays(amount);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * MySQL TIMESTAMPDIFF(unit, start, end) = end - start。
     * unit ∈ SECOND/MINUTE/HOUR/DAY/WEEK/MONTH/QUARTER/YEAR。
     */
    @SqlFunction("TIMESTAMPDIFF")
    public static Object timestampdiff(Map<String, Object> row, Object unit, Object start, Object end) {
        if (unit == null || start == null || end == null) return null;
        String u = unit.toString().trim().toUpperCase();
        java.time.LocalDateTime s = toLocalDateTime(start);
        java.time.LocalDateTime e = toLocalDateTime(end);
        switch (u) {
            case "SECOND": return java.time.temporal.ChronoUnit.SECONDS.between(s, e);
            case "MINUTE": return java.time.temporal.ChronoUnit.MINUTES.between(s, e);
            case "HOUR": return java.time.temporal.ChronoUnit.HOURS.between(s, e);
            case "DAY": return java.time.temporal.ChronoUnit.DAYS.between(s, e);
            case "WEEK": return java.time.temporal.ChronoUnit.WEEKS.between(s, e);
            case "MONTH": return java.time.temporal.ChronoUnit.MONTHS.between(s, e);
            case "QUARTER": return java.time.temporal.ChronoUnit.MONTHS.between(s, e) / 3;
            case "YEAR": return java.time.temporal.ChronoUnit.YEARS.between(s, e);
            default:
                throw new SqlException("TIMESTAMPDIFF 不支持的单位: " + u);
        }
    }

    /**
     * 时间分桶一等公民：{@code BUCKET(date_col, 'DAY'|'HOUR'|'WEEK'|'MONTH'|'YEAR'|'MINUTE')}。
     * 产出字符串桶键，可直接 {@code GROUP BY BUCKET(ts, 'DAY')}；比对 {@code DATE_FORMAT} + 各种标量函数别名更稳。
     */
    @SqlFunction("BUCKET")
    public static Object bucket(Map<String, Object> row, Object date, Object unit) {
        if (date == null || unit == null) {
            return null;
        }
        String u = unit.toString().toUpperCase();
        switch (u) {
            case "YEAR":
                return String.valueOf(toLocalDateTime(date).getYear());
            case "MONTH":
                return toLocalDateTime(date).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "WEEK": {
                java.time.LocalDate ld = toLocalDate(date);
                java.time.temporal.WeekFields wf = java.time.temporal.WeekFields.ISO;
                int yr = ld.get(wf.weekBasedYear());
                int wk = ld.get(wf.weekOfWeekBasedYear());
                return String.format("%d-W%02d", yr, wk);
            }
            case "DAY":
                return toLocalDate(date).toString();
            case "HOUR":
                return toLocalDateTime(date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"));
            case "MINUTE":
                return toLocalDateTime(date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            default:
                return toLocalDateTime(date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    /**
     * 宽松日期串解析：'2026-08-21'（纯日期→当日零点）与
     * '2026-08-21 11:00:26'（空格分隔，MySQL 习惯）都接受。
     */
    private static java.time.LocalDateTime toLocalDateTime(Object v) {
        if (v instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) v;
        }
        if (v instanceof java.time.LocalDate) {
            return ((java.time.LocalDate) v).atStartOfDay();
        }
        if (v instanceof java.util.Date) {
            return ((java.util.Date) v).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        String s = v.toString().trim();
        if (s.length() == 10) {
            return java.time.LocalDate.parse(s).atStartOfDay();
        }
        if (s.length() > 10 && s.charAt(10) == ' ') {
            s = s.substring(0, 10) + 'T' + s.substring(11);
        }
        return java.time.LocalDateTime.parse(s);
    }

    private static java.time.LocalDate toLocalDate(Object v) {
        if (v instanceof java.time.LocalDate) {
            return (java.time.LocalDate) v;
        }
        if (v instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) v).toLocalDate();
        }
        if (v instanceof java.util.Date) {
            return ((java.util.Date) v).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        String s = v.toString();
        return java.time.LocalDate.parse(s.length() >= 10 ? s.substring(0, 10) : s);
    }

    // ===================== 条件/空值函数 =====================

    @SqlFunction("IF")
    public static Object iff(Map<String, Object> row, Object cond, Object trueVal, Object falseVal) {
        return isTrue(cond) ? trueVal : falseVal;
    }

    @SqlFunction("IFNULL")
    public static Object ifnull(Map<String, Object> row, Object v, Object defaultVal) {
        return v != null ? v : defaultVal;
    }

    @SqlFunction("NULLIF")
    public static Object nullif(Map<String, Object> row, Object a, Object b) {
        // Compare by numeric value when both are Numbers, otherwise use Objects.equals
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue() ? null : a;
        }
        return Objects.equals(a, b) ? null : a;
    }

    @SqlFunction("COALESCE")
    public static Object coalesce(Map<String, Object> row, Object... args) {
        for (Object arg : args) {
            if (arg != null) return arg;
        }
        return null;
    }

    @SqlFunction("NVL")
    public static Object nvl(Map<String, Object> row, Object v, Object defaultVal) {
        return v != null ? v : defaultVal;
    }

    @SqlFunction("NVL2")
    public static Object nvl2(Map<String, Object> row, Object v, Object ifNotNull, Object ifNull) {
        return v != null ? ifNotNull : ifNull;
    }

    // ===================== 类型转换函数 =====================

    @SqlFunction("CAST")
    public static Object cast(Map<String, Object> row, Object v, Object asType) {
        if (v == null) return null;
        if (asType == null) return v;
        String t = asType.toString().toUpperCase().replace("AS ", "").trim();
        try {
            switch (t) {
                case "INTEGER":
                case "INT":
                    return Integer.parseInt(v.toString().trim().split("\\.")[0]);
                case "BIGINT":
                    return Long.parseLong(v.toString().trim().split("\\.")[0]);
                case "DOUBLE":
                case "FLOAT":
                case "REAL":
                    return Double.parseDouble(v.toString());
                case "DECIMAL":
                case "NUMERIC":
                    return new BigDecimal(v.toString());
                case "VARCHAR":
                case "CHAR":
                case "STRING":
                    return v.toString();
                case "BOOLEAN":
                case "BOOL":
                    String sv = v.toString().toLowerCase();
                    return "true".equals(sv) || "1".equals(sv) || "t".equals(sv);
                case "DATE":
                    return LocalDate.parse(v.toString().substring(0, 10));
                case "DATETIME":
                case "TIMESTAMP":
                    return LocalDateTime.parse(v.toString());
                default:
                    return v;
            }
        } catch (Exception e) {
            throw new SqlException("Cannot CAST '" + v + "' to " + t, e);
        }
    }

    @SqlFunction("TO_NUMBER")
    public static Object to_number(Map<String, Object> row, Object s) {
        if (s == null) return null;
        try {
            if (s instanceof Number) return s;
            return Double.parseDouble(s.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SqlFunction("TO_CHAR")
    public static Object to_char(Map<String, Object> row, Object v, Object fmt) {
        if (v == null) return null;
        if (fmt != null) return LocalDateTime.parse(v.toString()).format(DateTimeFormatter.ofPattern(fmt.toString()));
        return v.toString();
    }

    @SqlFunction("TO_DATE")
    public static Object to_date(Map<String, Object> row, Object s, Object fmt) {
        if (s == null) return null;
        try {
            if (fmt != null) {
                return java.time.LocalDate.parse(s.toString(), DateTimeFormatter.ofPattern(fmt.toString()));
            }
            return java.time.LocalDate.parse(s.toString().substring(0, 10));
        } catch (Exception e) {
            return s;
        }
    }

    // ===================== 聚合相关（可在表达式求值中引用） =====================

    @SqlFunction("SUM")
    public static Object sum(Map<String, Object> row, Object v) {
        if (v == null) return null;
        return v instanceof Number ? v : Double.parseDouble(v.toString());
    }

    @SqlFunction("AVG")
    public static Object avg(Map<String, Object> row, Object v) {
        return sum(row, v); // 具体聚合在执行器层处理
    }

    @SqlFunction("COUNT")
    public static Object count(Map<String, Object> row, Object v) {
        return 1L;
    }

    // ===================== 杂项 =====================

    @SqlFunction("ISNULL")
    public static Object isnull(Map<String, Object> row, Object v) {
        return v == null;
    }

    @SqlFunction("IS_NOT_NULL")
    public static Object is_not_null(Map<String, Object> row, Object v) {
        return v != null;
    }

    @SqlFunction("DECODE")
    public static Object decode(Map<String, Object> row, Object val, Object... pairs) {
        if (val == null || pairs == null || pairs.length == 0) return null;
        for (int i = 0; i < pairs.length - 1; i += 2) {
            // Coerce numeric types for comparison
            if (pairs[i] instanceof Number && val instanceof Number) {
                if (((Number) val).doubleValue() == ((Number) pairs[i]).doubleValue()) return pairs[i + 1];
            } else if (Objects.equals(val, pairs[i])) {
                return pairs[i + 1];
            }
        }
        // if odd number, last is default
        if (pairs.length % 2 == 1) return pairs[pairs.length - 1];
        return null;
    }

    @SqlFunction("GREATEST")
    public static Object greatest(Map<String, Object> row, Object... args) {
        return max(row, args);
    }

    @SqlFunction("LEAST")
    public static Object least(Map<String, Object> row, Object... args) {
        return min(row, args);
    }

    @SqlFunction("UUID")
    public static Object uuid(Map<String, Object> row, Object... args) {
        return UUID.randomUUID().toString();
    }

    @SqlFunction("MD5")
    public static Object md5(Map<String, Object> row, Object s) {
        if (s == null) return null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @SqlFunction("SHA1")
    public static Object sha1(Map<String, Object> row, Object s) {
        if (s == null) return null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(s.toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== 辅助方法 =====================

    private static boolean isTrue(Object cond) {
        if (cond == null) return false;
        if (cond instanceof Boolean) return (Boolean) cond;
        String s = cond.toString().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "t".equals(s);
    }
}