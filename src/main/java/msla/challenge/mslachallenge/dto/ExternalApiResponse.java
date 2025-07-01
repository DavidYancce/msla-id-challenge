package msla.challenge.mslachallenge.dto;

import java.math.BigDecimal;

public class ExternalApiResponse {

    private String date;
    private Info info;
    private Query query;
    private BigDecimal result;
    private boolean success;

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public BigDecimal getResult() {
        return result;
    }

    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Inner classes for nested objects
    public static class Info {
        private BigDecimal rate;
        private Long timestamp;

        public BigDecimal getRate() {
            return rate;
        }

        public void setRate(BigDecimal rate) {
            this.rate = rate;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class Query {
        private BigDecimal amount;
        private String from;
        private String to;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    @Override
    public String toString() {
        return "ExternalApiResponse{" +
                "date='" + date + '\'' +
                ", info=" + info +
                ", query=" + query +
                ", result=" + result +
                ", success=" + success +
                '}';
    }
}