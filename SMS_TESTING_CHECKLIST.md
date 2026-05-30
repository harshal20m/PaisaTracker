# SMS Feature Testing Checklist

## Pre-Build Verification ✅

- [x] Parser-core module copied (476 files)
- [x] Database entities created
- [x] Database DAOs created
- [x] Database migration added (v14 → v15)
- [x] Repositories created
- [x] SMS processor created
- [x] SMS broadcast receiver created
- [x] AndroidManifest updated with permissions
- [x] AndroidManifest updated with receiver registration
- [x] MainActivity updated with permission requests
- [x] MainActivity updated with intent handling
- [x] Build configuration updated (settings.gradle.kts)
- [x] Build configuration updated (build.gradle.kts)

## Build Testing

### Expected Build Result
- [ ] Build completes successfully
- [ ] No compilation errors
- [ ] APK generated in `build/outputs/apk/debug/`

### Common Build Issues to Check
- [ ] Kotlin version compatibility
- [ ] Room database version
- [ ] Missing dependencies
- [ ] Package name conflicts

## Installation Testing

### Device Requirements
- Android 9.0 (API 28) or higher
- SMS capability
- Notification support

### Installation Steps
1. [ ] Install APK on device
2. [ ] Launch app successfully
3. [ ] Complete first-time setup
4. [ ] Grant camera/storage permissions (existing)

## Permission Testing

### SMS Permissions
1. [ ] App requests RECEIVE_SMS permission
2. [ ] App requests READ_SMS permission
3. [ ] User can grant permissions
4. [ ] User can deny permissions
5. [ ] App handles permission denial gracefully

### Notification Permissions (Android 13+)
1. [ ] App requests POST_NOTIFICATIONS permission
2. [ ] User can grant/deny
3. [ ] Notifications work when granted

## Functional Testing

### SMS Detection
1. [ ] Send test SMS from supported bank format
2. [ ] Verify SMS is intercepted by receiver
3. [ ] Check Logcat for "SmsBroadcastReceiver" logs
4. [ ] Verify parser is found for sender

### Transaction Parsing
1. [ ] SMS is parsed correctly
2. [ ] Amount extracted correctly
3. [ ] Merchant name extracted
4. [ ] Transaction type identified (EXPENSE)
5. [ ] Date/timestamp captured

### Expense Creation
1. [ ] Expense is created in database
2. [ ] Category is assigned automatically
3. [ ] Amount matches SMS
4. [ ] Description contains merchant name
5. [ ] Payment method set to bank name

### Notification Display
1. [ ] Notification appears after SMS detection
2. [ ] Notification shows transaction amount
3. [ ] Notification shows merchant name
4. [ ] Notification is tappable
5. [ ] Tapping notification opens app

### Duplicate Prevention
1. [ ] Send same SMS twice
2. [ ] Verify only one expense is created
3. [ ] Check logs for "Duplicate transaction" message

### Unrecognized SMS
1. [ ] Send SMS from unknown sender
2. [ ] Verify it's stored in unrecognized_sms table
3. [ ] Check it doesn't create expense

## Database Testing

### Schema Verification
```sql
-- Check tables exist
SELECT name FROM sqlite_master WHERE type='table' AND name='bank_notifications';
SELECT name FROM sqlite_master WHERE type='table' AND name='unrecognized_sms';

-- Check data
SELECT * FROM bank_notifications;
SELECT * FROM unrecognized_sms;
SELECT * FROM expenses ORDER BY date DESC LIMIT 5;
```

### Migration Testing
1. [ ] Fresh install works (creates v15 database)
2. [ ] Upgrade from v14 works (migration runs)
3. [ ] No data loss during migration
4. [ ] Indices created correctly

## Integration Testing

### With Existing Features
1. [ ] Existing expense creation still works
2. [ ] Existing categories still work
3. [ ] Existing projects still work
4. [ ] App lock still works
5. [ ] Widgets still work
6. [ ] Backup/restore still works

### Multi-Part SMS
1. [ ] Send long SMS (split into parts)
2. [ ] Verify parts are combined correctly
3. [ ] Single expense created

## Performance Testing

### Memory
1. [ ] Check memory usage with SMS detection
2. [ ] No memory leaks
3. [ ] Background processing efficient

### Battery
1. [ ] Monitor battery usage
2. [ ] SMS receiver doesn't drain battery
3. [ ] Background work optimized

## Edge Cases

### Error Scenarios
1. [ ] SMS with invalid format
2. [ ] SMS with missing amount
3. [ ] SMS with missing merchant
4. [ ] Very long SMS body
5. [ ] Special characters in SMS
6. [ ] Multiple SMS at same time

### Permission Scenarios
1. [ ] Revoke SMS permission after granting
2. [ ] Grant permission after initial denial
3. [ ] App behavior without permissions

### Database Scenarios
1. [ ] Database full
2. [ ] Corrupted database
3. [ ] Migration failure

## Supported Banks Testing

### Indian Banks (Sample)
- [ ] HDFC Bank
- [ ] ICICI Bank
- [ ] SBI
- [ ] Axis Bank
- [ ] Kotak Bank

### International Banks (Sample)
- [ ] Chase (USA)
- [ ] Citibank (USA)
- [ ] ADCB (UAE)
- [ ] Bangkok Bank (Thailand)

## Test SMS Formats

### HDFC Bank
```
Dear Customer, your A/c XX1234 is debited with Rs.500.00 on 30-05-26 at AMAZON. Avl Bal: Rs.10000.00
```

### ICICI Bank
```
Your A/c XX5678 debited with INR 250.00 on 30-May-26 for SWIGGY. Avl bal INR 5000.00
```

### SBI
```
Dear Customer, INR 1000.00 debited from A/c XX9012 on 30-05-26 to FLIPKART. Avl Bal: INR 15000.00
```

## Regression Testing

### After Each Fix
1. [ ] Re-run all functional tests
2. [ ] Verify no new issues introduced
3. [ ] Check existing features still work

## User Acceptance Testing

### User Scenarios
1. [ ] User receives bank SMS
2. [ ] User sees notification
3. [ ] User taps notification
4. [ ] User views expense
5. [ ] User edits expense if needed
6. [ ] User is satisfied with auto-detection

## Documentation Verification

1. [ ] README updated
2. [ ] SMS_FEATURE_DOCUMENTATION.md accurate
3. [ ] Code comments present
4. [ ] Architecture documented

## Final Checklist

- [ ] All tests passed
- [ ] No critical bugs
- [ ] Performance acceptable
- [ ] User experience smooth
- [ ] Documentation complete
- [ ] Ready for production

## Known Limitations

1. Only EXPENSE transactions supported (not income/transfers)
2. No UI for reviewing pending transactions
3. No bulk SMS scanning
4. No custom merchant-category mapping UI
5. No bank account linking

## Future Enhancements

1. Add UI screens for SMS review
2. Support income/transfer transactions
3. Add bulk SMS scanning
4. Add merchant-category mapping
5. Add bank account linking
6. Add multi-currency support

---

**Testing Status**: ⏳ Awaiting build completion

**Last Updated**: 2026-05-30