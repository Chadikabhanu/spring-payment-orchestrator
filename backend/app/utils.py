import random
import string
import re
from datetime import datetime

def generate_id(prefix: str) -> str:
    """Generates an ID: prefix + 16 alphanumeric characters"""
    chars = string.ascii_letters + string.digits
    random_str = ''.join(random.choices(chars, k=16))
    return f"{prefix}_{random_str}"

def validate_vpa(vpa: str) -> bool:
    """Validates UPI VPA format: user@bank"""
    if not vpa:
        return False
    pattern = r"^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$"
    return bool(re.match(pattern, vpa))

def validate_luhn(card_number: str) -> bool:
    """Validates card number using Luhn algorithm"""
    if not card_number:
        return False
    cleaned = card_number.replace(" ", "").replace("-", "")
    if not cleaned.isdigit() or not (13 <= len(cleaned) <= 19):
        return False
    total = 0
    reverse_digits = cleaned[::-1]
    for i, digit in enumerate(reverse_digits):
        n = int(digit)
        if i % 2 == 1:
            n *= 2
            if n > 9:
                n -= 9
        total += n
    return total % 10 == 0

def get_card_network(card_number: str) -> str:
    """Detects card network based on prefix"""
    cleaned = card_number.replace(" ", "").replace("-", "")
    if cleaned.startswith("4"):
        return "visa"
    elif 51 <= int(cleaned[:2]) <= 55:
        return "mastercard"
    elif cleaned[:2] in ["34", "37"]:
        return "amex"
    elif cleaned[:2] in ["60", "65"] or 81 <= int(cleaned[:2]) <= 89:
        return "rupay"
    else:
        return "unknown"

def validate_expiry(month: str, year: str) -> bool:
    """Validates if card is not expired"""
    try:
        m = int(month)
        y = int(year)
        
        # Validate month range
        if not (1 <= m <= 12):
            return False
            
        # Handle 2-digit year (assume 20xx)
        if y < 100:
            y += 2000
            
        current_date = datetime.now()
        current_year = current_date.year
        current_month = current_date.month
        
        # Check if future
        if y > current_year:
            return True
        elif y == current_year and m >= current_month:
            return True
        return False
    except ValueError:
        return False