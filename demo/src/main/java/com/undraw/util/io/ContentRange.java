package com.undraw.util.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentRange {

    /**
     * 第一个字节的位置
     */
    private final long start;

    /**
     * 最后一个字节的位置
     */
    private long end;

    /**
     * 内容完整的长度/总长度
     */
    private final long length;

    public static final String BYTES_STRING = "bytes";

    /**
     * 组装内容范围的响应头。
     * <pre>
     * <a href="https://www.rfc-editor.org/rfc/rfc7233#section-4.2">
     *     4.2. Content-Range - HTTP/1.1 Range Requests</a>
     * Content-Range: "bytes" first-byte-pos "-" last-byte-pos  "/" complete-length
     *
     * For example:
     * Content-Range: bytes 0-499/1234
     * </pre>
     *
     * @return 内容范围的响应头
     */
    public String toContentRange() {
        return BYTES_STRING + ' ' + start + '-' + end + '/' + length;
    }

    /**
     * 计算内容完整的长度/总长度。
     *
     * @return 内容完整的长度/总长度
     */
    public long applyAsContentLength() {
        return end - start + 1;
    }

    /**
     * Validate range.
     *
     * @return true if the range is valid, otherwise false
     */
    public boolean validate() {
        if (end >= length) {
            end = length - 1;
        }
        return (start >= 0) && (end >= 0) && (start <= end) && (length > 0);
    }

    @Override
    public String toString() {
        return "firstBytePos=" + start +
                ", lastBytePos=" + end +
                ", fileLength=" + length;
    }
}