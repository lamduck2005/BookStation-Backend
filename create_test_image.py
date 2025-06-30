#!/usr/bin/env python3
import base64
from PIL import Image
import io

# Create a simple 300x300 red square image
img = Image.new('RGB', (300, 300), color='red')

# Save as PNG in memory
buffer = io.BytesIO()
img.save(buffer, format='PNG')
buffer.seek(0)

# Save to file
with open('test-image.png', 'wb') as f:
    f.write(buffer.getvalue())

print("Created test-image.png (300x300 red square)")

# Also create base64 data URL for HTML testing
buffer.seek(0)
img_data = buffer.getvalue()
b64_data = base64.b64encode(img_data).decode()
data_url = f"data:image/png;base64,{b64_data}"

print(f"Base64 data URL length: {len(data_url)} characters")
print("First 100 chars:", data_url[:100])
