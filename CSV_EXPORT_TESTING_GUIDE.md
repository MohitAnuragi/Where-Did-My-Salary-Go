# 🧪 CSV Export Testing Guide

## Quick Test Steps

### ✅ Test 1: Free User Cannot Export

**Steps:**
1. Open app
2. Go to **Settings** screen
3. Scroll down to **"Data Export"** section
4. Tap **"Export Data"** button

**Expected Result:**
- ❌ No file is generated
- ✅ Toast appears: "Upgrade to Pro to export data"
- ✅ Button returns to normal state
- ✅ No crash

---

### ✅ Test 2: Pro User Can Export

**Pre-requisite:** Activate Pro subscription first
- Go to Settings → Notifications → "Upgrade" button
- OR use Pro activation screen

**Steps:**
1. As Pro user, add some test data:
   - Set salary: ₹50,000
   - Add expense: Rent - ₹10,000 - Due: 5th
   - Add expense: EMI - ₹6,000 - Due: 10th
   - Add expense: Netflix - ₹199 - Due: 15th
2. Go to **Settings**
3. Scroll to **"Data Export"**
4. Tap **"Export Data"**

**Expected Result:**
- ✅ Button shows "Exporting..." with spinner
- ✅ System Share Sheet appears after 1-2 seconds
- ✅ Toast appears: "Data exported successfully"
- ✅ File name: `WhereDidMySalaryGo_Export_Feb_2026.csv`

---

### ✅ Test 3: CSV Opens in Apps

**Steps:**
1. After exporting, from Share Sheet choose:
   - **Option A:** "Save to Files" → Open with Excel/Sheets
   - **Option B:** "Open in Google Sheets"
   - **Option C:** "Gmail" → Attach and send to yourself

**Expected Result:**
- ✅ File opens without errors
- ✅ Headers visible in first row
- ✅ Data is readable and properly formatted
- ✅ No weird characters (UTF-8 works)

**CSV Should Look Like:**
```
Month,Salary,Total Fixed Expenses,Remaining Amount,Expense Name,Expense Amount,Due Date
Feb 2026,50000.00,16199.00,33801.00,Rent,10000.00,5
,,,,,EMI,6000.00,10
,,,,,Netflix,199.00,15
```

---

### ✅ Test 4: Empty Data Handling

**Steps:**
1. Delete all expenses (swipe to delete in Expense List)
2. Go to Settings
3. Export data

**Expected Result:**
- ✅ Export succeeds (no crash)
- ✅ CSV file created
- ✅ CSV contains headers only
- ✅ Share sheet opens

**CSV Content:**
```
Month,Salary,Total Fixed Expenses,Remaining Amount,Expense Name,Expense Amount,Due Date
Feb 2026,50000.00,0.00,50000.00,,,
```

---

### ✅ Test 5: Special Characters

**Steps:**
1. Add expense with special name: `Rent (1st Floor), "Main" Building`
2. Export data
3. Open CSV in Excel/Sheets

**Expected Result:**
- ✅ Name is properly escaped
- ✅ Commas and quotes don't break CSV format
- ✅ Data displays correctly

---

### ✅ Test 6: Subscription Plan Limits

**Test Different Plans:**

**Plan: PRO_1_MONTH**
- Export range: Last 1 month
- Manual test: Check CSV only has current month

**Plan: PRO_6_MONTH**
- Export range: Last 3 months
- Manual test: Check CSV has 3 months

**Plan: PRO_1_YEAR**
- Export range: Last 6 months
- Manual test: Check CSV has 6 months

**How to Set Plan (Temporary for Testing):**
```kotlin
// In ProViewModel or test code:
userPreferencesManager.setSubscriptionPlan("PRO_6_MONTH")
```

---

### ✅ Test 7: Orientation Change Safety

**Steps:**
1. Tap "Export Data"
2. **Immediately** rotate device while export is in progress
3. Observe behavior

**Expected Result:**
- ✅ No crash
- ✅ Export completes successfully
- ✅ Share sheet still appears
- ✅ ViewModel handles state correctly

---

### ✅ Test 8: Multiple Exports

**Steps:**
1. Export data
2. Complete sharing (or cancel)
3. Immediately export again
4. Repeat 3-4 times

**Expected Result:**
- ✅ Each export works
- ✅ New file created each time (same name overwrites in cache)
- ✅ No memory leaks
- ✅ No performance degradation

---

## 🐛 Known Issues to Check

### Issue: Button stays in "Exporting..." state
**Cause:** Export failed but UI not updated
**Fix:** Check error handling in SettingsViewModel.exportData()

### Issue: Share sheet doesn't open
**Cause:** FileProvider not configured
**Fix:** Check AndroidManifest.xml has FileProvider declaration

### Issue: "Permission denied" error
**Cause:** Missing GRANT_URI_PERMISSION flag
**Fix:** Already handled in ExportManager.shareFile()

### Issue: CSV shows weird characters
**Cause:** Wrong encoding
**Fix:** Already using UTF-8 in CsvExporter

---

## 📱 Test on Multiple Devices

**Minimum:** Android 8 (API 26)
**Recommended:** Test on:
- Android 8-9 (older devices)
- Android 10-11 (scoped storage)
- Android 12+ (Material You)
- Android 13+ (notification permissions)

---

## ✅ Final Validation

Before marking as complete, verify:

- [ ] Free users cannot export (Toast shown)
- [ ] Pro users can export successfully
- [ ] CSV file opens in Excel/Google Sheets
- [ ] Empty data doesn't crash
- [ ] Special characters are escaped
- [ ] Share sheet appears
- [ ] Toast "Data exported successfully" shows
- [ ] Button loading state works
- [ ] Orientation change is safe
- [ ] Multiple exports work
- [ ] File name has correct format
- [ ] CSV has proper headers
- [ ] Data is accurate and complete

---

## 🚀 Production Checklist

Before releasing to users:

1. [ ] Test with real user data (100+ expenses)
2. [ ] Test on low-end devices
3. [ ] Test with slow storage
4. [ ] Test with external storage permission issues
5. [ ] Test all subscription plans
6. [ ] Verify no personal data leaks in CSV
7. [ ] Check CSV file size is reasonable
8. [ ] Ensure export doesn't block UI
9. [ ] Add analytics for export success/failure rates
10. [ ] Document feature in user guide

---

## 📞 Support Questions

**Q: Where is the exported file saved?**
**A:** In app cache directory. Use Share Sheet to save to desired location.

**Q: Can I export to PDF?**
**A:** No, CSV only per requirements.

**Q: Can I choose custom date range?**
**A:** No, date range is based on subscription plan.

**Q: Does export work offline?**
**A:** Yes, completely offline. Only sharing requires internet if sending via email/cloud.

**Q: What if I have no data?**
**A:** Export succeeds with headers only.

---

**Testing Status:** Ready for QA ✅

